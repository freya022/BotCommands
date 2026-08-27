package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.LocalePreference
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.PreferLocale
import dev.freya02.botcommands.typesafe.messages.api.exceptions.InvalidSourceException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnsupportedFunctionException
import dev.freya02.botcommands.typesafe.messages.api.exceptions.UnsupportedParameterException
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.*
import dev.freya02.botcommands.typesafe.messages.internal.exceptions.throwInternal
import dev.freya02.botcommands.typesafe.messages.internal.utils.*
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.localization.arguments.FormattableArgument
import io.github.freya022.botcommands.internal.core.restarter.RestartClassLoaderAdapter
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.slf4j.LoggerFactory
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.TypeKind
import java.lang.classfile.attribute.SourceFileAttribute
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

internal object MessageSourceGenerator {

    internal fun create(sourceType: KClass<out IMessageSource>): MethodHandle {
        require(sourceType.java.isInterface, ::InvalidSourceException) {
            "${sourceType.jvmName} must be an interface!"
        }

        val abstractMethods = sourceType.functions.filter { it.isAbstract }
        val toImplement = getImplementableFunctions(sourceType)

        (abstractMethods - toImplement).also { unimplementedMethods ->
            require(unimplementedMethods.isEmpty(), ::InvalidSourceException) {
                "Abstract methods in ${sourceType.jvmName} can only be implemented if annotated with @${LocalizedContent::class.java.simpleName}:\n${unimplementedMethods.joinAsList()}"
            }
        }

        val classFile = ClassFile.of()
        val thisClass = ClassDesc.of("${MessageSourceGenerator::class.java.packageName}.${sourceType.simpleNestedBinaryName}Impl")
        val sourceBytes = classFile.build(thisClass) { classBuilder ->
            classBuilder.with(SourceFileAttribute.of(thisClass.displayName()))
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withInterfaceSymbols(ClassDesc.of(sourceType.jvmName))

            classBuilder.withField("messageSourceContext", CD_MessageSourceContext, ClassFile.ACC_PRIVATE or ClassFile.ACC_FINAL)

            classBuilder.withMethodBody(
                INIT_NAME,
                MethodTypeDesc.of(CD_void, CD_MessageSourceContext),
                ClassFile.ACC_PUBLIC
            ) { codeBuilder ->
                val lineNumber = LineNumber(codeBuilder)

                val thisSlot = codeBuilder.receiverSlot()
                val messageSourceContextSlot = codeBuilder.parameterSlot(0)

                // this.super()
                lineNumber.setAndIncrement()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void))

                // this.messageSourceContext = messageSourceContext
                lineNumber.setAndIncrement()
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(messageSourceContextSlot)
                codeBuilder.putfield(thisClass, "messageSourceContext", CD_MessageSourceContext)

                // Required
                codeBuilder.return_()
            }

            toImplement.forEach { function ->
                LocalizedContentFunctionGenerator.create(thisClass, classBuilder, sourceType, function)
            }
        }

        return when (val restartLoader = RestartClassLoaderAdapter.wrapOrNull(sourceType.java.classLoader)) {
            null -> {
                val lookup = MethodHandles.lookup()
                lookup.defineClass(sourceBytes)
                    .getConstructor(MessageSourceContext::class.java)
                    .let(lookup::unreflectConstructor)
            }

            else -> {
                val lookup = MethodHandles.publicLookup()
                restartLoader.publicDefineClass("${thisClass.packageName()}.${thisClass.displayName()}", sourceBytes)
                    .declaredConstructors.single()
                    .let(lookup::unreflectConstructor)
            }
        }
    }

    internal fun getImplementableFunctions(sourceType: KClass<out IMessageSource>): List<KFunction<*>> {
        return sourceType.functions
            .filter { it.isAbstract }
            .filter { it.hasAnnotation<LocalizedContent>() }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : IMessageSource> instantiate(handle: MethodHandle, messageSourceContext: MessageSourceContext): T {
        return handle.invoke(messageSourceContext) as T
    }
}

internal object LocalizedContentFunctionGenerator {

    private val logger = KotlinLogging.logger { }

    internal fun create(thisClass: ClassDesc, classBuilder: ClassBuilder, declaringClass: KClass<out IMessageSource>, function: KFunction<*>) {
        require(!function.isSuspend, ::UnsupportedFunctionException) {
            "Suspend functions are not supported! ${function.getSignature(qualifiedClass = true, source = false)}"
        }

        require(function.returnType.jvmErasure == String::class, ::UnsupportedFunctionException) {
            "Function must return a String: ${function.getSignature(qualifiedClass = true, source = false)}"
        }

        val annotation = function.findAnnotation<LocalizedContent>()
            ?: error("Function was to be implemented but annotation is absent")

        val nullableIndexes = NullabilityHelper.getNullableParameterIndexes(logger, declaringClass, function)
        // -1 to account for the instance parameter (which is always present as all functions are interface methods)
        fun isNullable(parameter: KParameter): Boolean = parameter.index - 1 in nullableIndexes

        val templateParameters = getTemplateArgumentParameters(function).onEach { parameter ->
            require(parameter.isRequired, ::UnsupportedParameterException) {
                "Optional parameters are not supported! $parameter"
            }

            require(!isNullable(parameter), ::UnsupportedParameterException) {
                "Nullable parameters are not allowed! $parameter"
            }

            require(!parameter.type.jvmErasure.java.isBoxedPrimitive(), ::UnsupportedParameterException) {
                "Boxed primitive parameters are not allowed! $parameter"
            }
        }
        val localeParameter = getLocaleParameter(function)

        // Check for parameter unhandled by this generator
        val missedParameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE } - templateParameters - localeParameter
        check(missedParameters.isEmpty()) {
            "Some parameters are not supported!\n${missedParameters.joinAsList()}"
        }

        val preferredLocale = function.findAnnotation<PreferLocale>()?.preference ?: declaringClass.findAnnotation<PreferLocale>()?.preference
        if (preferredLocale != null && localeParameter != null && !isNullable(localeParameter)) {
            // If there is a preferred locale annotation, it makes no sense to also have a mandatory locale parameter
            val logger = LoggerFactory.getLogger(declaringClass.java)
            logger.warn("@${PreferLocale::class.java.simpleName} is ignored on ${function.getSignature(source = false)} because it has a non-null ${localeParameter.type.jvmErasure.simpleName} parameter")
        }

        classBuilder.withMethodBody(
            function.name,
            function.toMethodTypeDesc(),
            ClassFile.ACC_PUBLIC or ClassFile.ACC_FINAL,
        ) { codeBuilder ->
            val lineNumber = LineNumber(codeBuilder)

            val thisSlot = codeBuilder.receiverSlot()
            val localizationArgsSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
            val localizationEntrySlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

            // var localizationArgs = new Localization.Entry[templateParameters.size];
            lineNumber.setAndIncrement()
            codeBuilder.loadConstant(templateParameters.size)
            codeBuilder.anewarray(CD_Localization_Entry)
            codeBuilder.astore(localizationArgsSlot)

            templateParameters.forEachIndexed { arrayIndex, parameter ->
                val parameterIndex = parameter.index - 1 // 1st is instance parameter
                val templateVarName = getTemplateArgumentParameterName(parameter)
                    ?: error("Parameter names are absent from $function ; see https://bc.freya02.dev/3.X/setup/parameter-names/")

                // localizationEntry = new Localization.Entry(paramName, value)
                lineNumber.setAndIncrement()
                codeBuilder.new_(CD_Localization_Entry)
                codeBuilder.dup() // As <init> doesn't return itself
                codeBuilder.ldc(templateVarName)
                codeBuilder.loadLocal(TypeKind.from(parameter.type.jvmErasure.java), codeBuilder.parameterSlot(parameterIndex))
                codeBuilder.boxIfNecessary(parameter.type.jvmErasure)
                codeBuilder.invokespecial(CD_Localization_Entry, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String, CD_Object))
                codeBuilder.astore(localizationEntrySlot)

                // localizationArgs[i] = localizationEntry
                lineNumber.setAndIncrement()
                codeBuilder.aload(localizationArgsSlot)
                codeBuilder.loadConstant(arrayIndex)
                codeBuilder.aload(localizationEntrySlot)
                codeBuilder.aastore()
            }

            fun callWithContextLocale() {
                // return this.messageSourceContext.localizePreferringUser("<templateKey>", localizationArgs)
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(thisClass, "messageSourceContext", CD_MessageSourceContext)
                codeBuilder.ldc(annotation.templateKey)
                codeBuilder.aload(localizationArgsSlot)
                codeBuilder.invokevirtual(CD_MessageSourceContext, "localizePreferringUser", MethodTypeDesc.of(CD_String, CD_String, CD_Localization_Entry.arrayType()))
                codeBuilder.areturn()
            }

            fun callWithGuildLocale() {
                // return this.messageSourceContext.localizeWithGuild("<templateKey>", localizationArgs)
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(thisClass, "messageSourceContext", CD_MessageSourceContext)
                codeBuilder.ldc(annotation.templateKey)
                codeBuilder.aload(localizationArgsSlot)
                codeBuilder.invokevirtual(CD_MessageSourceContext, "localizeWithGuild", MethodTypeDesc.of(CD_String, CD_String, CD_Localization_Entry.arrayType()))
                codeBuilder.areturn()
            }

            fun callWith(localeDescriptor: ClassDesc, localeSlot: Int) {
                // return this.messageSourceContext.localizeWith(locale, "<templateKey>", localizationArgs)
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(thisClass, "messageSourceContext", CD_MessageSourceContext)
                codeBuilder.aload(localeSlot)
                codeBuilder.ldc(annotation.templateKey)
                codeBuilder.aload(localizationArgsSlot)
                codeBuilder.invokevirtual(CD_MessageSourceContext, "localizeWith", MethodTypeDesc.of(CD_String, localeDescriptor, CD_String, CD_Localization_Entry.arrayType()))
                codeBuilder.areturn()
            }

            lineNumber.setAndIncrement()
            if (localeParameter != null) {
                val localeDescriptor = when (val localeType = localeParameter.type.jvmErasure.java) {
                    DiscordLocale::class.java -> CD_DiscordLocale
                    Locale::class.java -> CD_Locale
                    else -> throwInternal("Unsupported locale type: ${localeType.name}")
                }
                val localeSlot = codeBuilder.parameterSlot(localeParameter.index - 1 /* instance */)
                val ifNullLocaleLabel = codeBuilder.newLabel()

                // if (locale == null) goto ifNullLocaleLabel;
                codeBuilder.aload(localeSlot)
                codeBuilder.ifnull(ifNullLocaleLabel)
                // Locale is not null
                callWith(localeDescriptor, localeSlot)

                // Locale is null
                codeBuilder.labelBinding(ifNullLocaleLabel)
                when (preferredLocale) {
                    LocalePreference.PREFER_USER, null -> callWithContextLocale()
                    LocalePreference.GUILD -> callWithGuildLocale()
                }
            } else {
                when (preferredLocale) {
                    LocalePreference.PREFER_USER, null -> callWithContextLocale()
                    LocalePreference.GUILD -> callWithGuildLocale()
                }
            }
        }
    }

    private fun Class<*>.isBoxedPrimitive(): Boolean {
        return when (this.name) {
            "java.lang.Boolean" -> true
            "java.lang.Character" -> true
            "java.lang.Byte" -> true
            "java.lang.Short" -> true
            "java.lang.Integer" -> true
            "java.lang.Float" -> true
            "java.lang.Long" -> true
            "java.lang.Double" -> true
            else -> false
        }
    }

    internal fun getTemplateArgumentParameters(function: KFunction<*>): List<KParameter> {
        var parameters = function.valueParameters

        // Exclude locale parameter
        val localeParameter = getLocaleParameter(function)
        if (localeParameter != null)
            parameters = parameters - localeParameter

        return parameters
    }

    internal fun getLocaleParameter(function: KFunction<*>): KParameter? {
        return function.valueParameters.firstOrNull()?.takeIf {
            val localeType = it.type.jvmErasure.java
            localeType == DiscordLocale::class.java || localeType == Locale::class.java
        }
    }

    internal fun getTemplateArgumentParameterName(parameter: KParameter): String? {
        return parameter.name?.convertToCamelCase()
    }

    internal fun getParameterTemplateArgumentName(argument: FormattableArgument): String {
        return argument.argumentName.convertToSnakeCase()
    }
}

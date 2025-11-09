package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.exceptions.*
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceContext
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.*
import dev.freya02.botcommands.typesafe.messages.internal.utils.convertToCamelCase
import dev.freya02.botcommands.typesafe.messages.internal.utils.isRequired
import dev.freya02.botcommands.typesafe.messages.internal.utils.require
import dev.freya02.botcommands.typesafe.messages.internal.utils.simpleNestedBinaryName
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.joinAsList
import net.dv8tion.jda.api.interactions.DiscordLocale
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
        require(sourceType.java.isInterface, ::IllegalMessageSourceClassTypeException) {
            "${sourceType.jvmName} must be an interface!"
        }

        val abstractMethods = sourceType.functions.filter { it.isAbstract }
        val toImplement = getImplementableFunctions(sourceType)

        (abstractMethods - toImplement).also { unimplementedMethods ->
            require(unimplementedMethods.isEmpty(), ::AbstractMessageSourceMethodException) {
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
                LocalizedContentFunctionGenerator.create(thisClass, classBuilder, function)
            }
        }

        val lookup = MethodHandles.lookup()
        return lookup.defineClass(sourceBytes)
            .getConstructor(MessageSourceContext::class.java)
            .let(lookup::unreflectConstructor)
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

    internal fun create(thisClass: ClassDesc, classBuilder: ClassBuilder, function: KFunction<*>) {
        require(!function.isSuspend, ::UnsupportedSuspendFunctionException) {
            "Suspend functions are not supported! ${function.getSignature(qualifiedClass = true, source = false)}"
        }

        require(function.returnType.jvmErasure == String::class, ::IllegalMessageSourceReturnTypeException) {
            "Function must return a String: ${function.getSignature(qualifiedClass = true, source = false)}"
        }

        val annotation = function.findAnnotation<LocalizedContent>()
            ?: error("Function was to be implemented but annotation is absent")
        val templateParameters = getTemplateArgumentParameters(function).onEach { parameter ->
            require(parameter.isRequired, ::UnsupportedOptionalParameterException) {
                "Optional parameters are not supported! $parameter"
            }

            require(!parameter.type.isMarkedNullable, ::UnsupportedNullableParameterException) {
                "Nullable parameters are not allowed! $parameter"
            }
        }
        val localeParameter = getLocaleParameter(function)

        // Check for parameter unhandled by this generator
        val missedParameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE } - templateParameters - localeParameter
        check(missedParameters.isEmpty()) {
            "Some parameters are not supported!\n${missedParameters.joinAsList()}"
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

            templateParameters.withIndex().forEachIndexed { arrayIndex, (parameterIndex, parameter) ->
                val templateVarName = getTemplateArgumentParameterName(parameter)
                    ?: error("Parameter names are absent from $function ; see https://bc.freya02.dev/3.X/using-botcommands/parameter-names/")

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

            lineNumber.setAndIncrement()
            if (localeParameter != null) {
                val localeSlot = codeBuilder.parameterSlot(localeParameter.index - 1 /* instance */)
                val ifNullLocaleLabel = codeBuilder.newLabel()

                // if (locale == null) goto ifNullLocaleLabel;
                codeBuilder.aload(localeSlot)
                codeBuilder.ifnull(ifNullLocaleLabel)
                // Locale is not null
                // return this.messageSourceContext.localizeWith(locale, "<templateKey>", localizationArgs)
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(thisClass, "messageSourceContext", CD_MessageSourceContext)
                codeBuilder.aload(localeSlot)
                codeBuilder.ldc(annotation.templateKey)
                codeBuilder.aload(localizationArgsSlot)
                codeBuilder.invokevirtual(CD_MessageSourceContext, "localizeWith", MethodTypeDesc.of(CD_String, CD_DiscordLocale, CD_String, CD_Localization_Entry.arrayType()))
                codeBuilder.areturn()

                // Locale is null
                codeBuilder.labelBinding(ifNullLocaleLabel)
                callWithContextLocale()
            } else {
                callWithContextLocale()
            }
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
        return function.valueParameters.firstOrNull()?.takeIf { it.type.jvmErasure.java == DiscordLocale::class.java }
    }

    internal fun getTemplateArgumentParameterName(parameter: KParameter): String? {
        return parameter.name?.convertToCamelCase()
    }
}

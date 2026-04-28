package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.exceptions.InvalidSourceFactoryException
import dev.freya02.botcommands.typesafe.messages.internal.MessageSourceFactoryProvider
import dev.freya02.botcommands.typesafe.messages.internal.TextCommandLocaleProviderAdapter
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.LineNumber
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.classDesc
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.createSignature
import dev.freya02.botcommands.typesafe.messages.internal.utils.isAbstract
import dev.freya02.botcommands.typesafe.messages.internal.utils.require
import dev.freya02.botcommands.typesafe.messages.internal.utils.simpleNestedBinaryName
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.core.restarter.RestartClassLoaderAdapter
import io.github.freya022.botcommands.internal.utils.superErasureAt
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import org.slf4j.LoggerFactory
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.*
import java.lang.classfile.attribute.SignatureAttribute
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.ConstantDescs.INIT_NAME
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

object MessageSourceFactoryGenerator {

    private val CD_AbstractMessageSourceFactory = classDesc<AbstractMessageSourceFactory<*>>()
    private val CD_AbstractMessageSourceFactory_Params = classDesc<AbstractMessageSourceFactory.Params>()

    fun <T : IMessageSourceFactory<*>> createProvider(
        annotation: MessageSourceFactory,
        sourceFactoryType: KClass<T>,
    ): MessageSourceFactoryProvider<T> {
        return createProvider(
            annotation.bundleName,
            annotation.discordLocales.toSet().unmodifiableView(),
            annotation.locales.mapTo(linkedSetOf(), Locale::forLanguageTag).unmodifiableView(),
            annotation.ignoreEmptyLocales,
            sourceFactoryType,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IMessageSourceFactory<*>> createProvider(
        bundleName: String,
        discordLocales: Set<DiscordLocale>,
        locales: Set<Locale>,
        ignoreEmptyLocales: Boolean,
        sourceFactoryType: KClass<T>,
    ): MessageSourceFactoryProvider<T> {
        require(sourceFactoryType.java.isInterface, ::InvalidSourceFactoryException) {
            "${sourceFactoryType.jvmName} must be an interface!"
        }

        // The only abstract method should be the one we implement
        sourceFactoryType.java.methods
            .asSequence()
            // Look at abstract methods
            .filter { it.isAbstract() }
            // Remove methods we implement
            .filterNot { it.name == "create" && it.parameterTypes.getOrNull(0) == Interaction::class.java && it.returnType == IMessageSource::class.java }
            .filterNot { it.name == "create" && it.parameterTypes.getOrNull(0) == MessageReceivedEvent::class.java && it.returnType == IMessageSource::class.java }
            .filterNot { it.name == "create" && it.parameterTypes.getOrNull(0) == Locale::class.java && it.parameterTypes.getOrNull(1) == Locale::class.java && it.returnType == IMessageSource::class.java }
            .filterNot { it.name == "getBundleName" && it.parameterTypes.isEmpty() }
            .filterNot { it.name == "getLocales" && it.parameterTypes.isEmpty() }
            .toList()
            .also { unimplementedMethods ->
                require(unimplementedMethods.isEmpty(), ::InvalidSourceFactoryException) {
                    "${sourceFactoryType.jvmName} cannot contain abstract methods:\n${unimplementedMethods.joinAsList()}"
                }
            }

        val effectiveLocales: Set<Locale> = run {
            if (discordLocales.isEmpty() && locales.isEmpty()) {
                if (!ignoreEmptyLocales) {
                    LoggerFactory.getLogger(sourceFactoryType.java).warn("No locales were provided for ${sourceFactoryType.shortQualifiedName}, if this is intentional (i.e. you don't provide translations), set 'ignoreEmptyLocales' to true")
                }
                return@run emptySet()
            }

            (discordLocales.mapTo(hashSetOf()) { it.toLocale() } + locales).unmodifiableView()
        }

        val sourceType = sourceFactoryType.superErasureAt<IMessageSourceFactory<*>>(0).jvmErasure as KClass<IMessageSource>

        val classFile = ClassFile.of()
        val thisClass = ClassDesc.of("${MessageSourceFactoryGenerator::class.java.packageName}.${sourceFactoryType.simpleNestedBinaryName}Impl")

        // Simple class that extends [[AbstractMessageSourceFactory]] and the target [[IMessageSourceFactory]] subinterface
        val factoryBytes = classFile.build(thisClass) { classBuilder ->
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withSuperclass(CD_AbstractMessageSourceFactory)
            classBuilder.withInterfaceSymbols(ClassDesc.of(sourceFactoryType.jvmName))
            classBuilder.with(SignatureAttribute.of(CD_AbstractMessageSourceFactory.createSignature(sourceType.java)))

            classBuilder.withField("params", CD_AbstractMessageSourceFactory_Params, ACC_PRIVATE or ACC_FINAL)

            classBuilder.withMethodBody(
                INIT_NAME,
                MethodTypeDesc.of(CD_void, CD_AbstractMessageSourceFactory_Params),
                ACC_PUBLIC
            ) { codeBuilder ->
                val lineNumber = LineNumber(codeBuilder)

                val thisSlot = codeBuilder.receiverSlot()
                val paramsSlot = codeBuilder.parameterSlot(0)

                // this.super(params)
                lineNumber.setAndIncrement()
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(paramsSlot)
                codeBuilder.invokespecial(CD_AbstractMessageSourceFactory, INIT_NAME, MethodTypeDesc.of(CD_void, CD_AbstractMessageSourceFactory_Params))

                // Required
                codeBuilder.return_()
            }
        }

        val factoryHandle: MethodHandle = when (val restartLoader = RestartClassLoaderAdapter.wrapOrNull(sourceFactoryType.java.classLoader)) {
            null -> {
                val lookup = MethodHandles.lookup()
                lookup.defineClass(factoryBytes)
                    .declaredConstructors.single()
                    .let(lookup::unreflectConstructor)
            }

            else -> {
                val lookup = MethodHandles.publicLookup()
                restartLoader.publicDefineClass("${thisClass.packageName()}.${thisClass.displayName()}", factoryBytes)
                    .declaredConstructors.single()
                    .let(lookup::unreflectConstructor)
            }
        }

        // Create it outside the factory to prevent duplicates and also validate early
        val sourceHandle = MessageSourceGenerator.create(sourceType)

        return MessageSourceFactoryProvider { context: BContext ->
            val params = AbstractMessageSourceFactory.Params(
                context.getService<LocalizationService>(),
                bundleName,
                effectiveLocales,
                context.getServiceOrNull<TextCommandLocaleProviderAdapter>(),
                context.getService<GuildLocaleProvider>(),
                context.getService<UserLocaleProvider>(),
                sourceHandle,
            )

            factoryHandle.invoke(params) as T
        }
    }
}

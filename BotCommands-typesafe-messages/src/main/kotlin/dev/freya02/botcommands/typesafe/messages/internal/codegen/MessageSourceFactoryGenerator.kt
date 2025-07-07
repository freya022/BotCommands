package dev.freya02.botcommands.typesafe.messages.internal.codegen

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.exceptions.AbstractMessageSourceFactoryMethodException
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.LineNumber
import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.classDesc
import dev.freya02.botcommands.typesafe.messages.internal.utils.isAbstract
import dev.freya02.botcommands.typesafe.messages.internal.utils.simpleNestedBinaryName
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import net.dv8tion.jda.api.interactions.Interaction
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.*
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.ConstantDescs.INIT_NAME
import java.lang.constant.MethodTypeDesc
import java.lang.invoke.MethodHandles
import java.lang.reflect.AccessFlag
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

internal object MessageSourceFactoryGenerator {

    private val CD_AbstractMessageSourceFactory = classDesc<AbstractMessageSourceFactory>()
    private val CD_AbstractMessageSourceFactory_Params = classDesc<AbstractMessageSourceFactory.Params>()

    internal fun createFactory(
        context: BContext,
        annotation: MessageSourceFactory,
        sourceFactoryType: KClass<out IMessageSourceFactory<*>>,
        sourceType: KClass<out IMessageSource>,
    ): IMessageSourceFactory<*> {
        // The only abstract method should be the one we implement
        sourceFactoryType.java.methods
            // Look at abstract methods
            .filter { it.isAbstract() }
            // Remove methods we implement
            .filterNot { it.name == "create" && it.parameterTypes.getOrNull(0) == Interaction::class.java && it.returnType == IMessageSource::class.java }
            .also { unimplementedMethods ->
                if (unimplementedMethods.isNotEmpty()) {
                    throw AbstractMessageSourceFactoryMethodException("${sourceFactoryType.jvmName} cannot contain abstract methods:\n${unimplementedMethods.joinAsList()}")
                }
            }

        val params = AbstractMessageSourceFactory.Params(
            context.getService<LocalizationService>(),
            annotation.bundleName,
            context.getService<GuildLocaleProvider>(),
            context.getService<UserLocaleProvider>(),
            sourceType,
        )

        val classFile = ClassFile.of()
        val thisClass = ClassDesc.of("${MessageSourceFactoryGenerator::class.java.packageName}.${sourceFactoryType.simpleNestedBinaryName}Impl")

        // Simple class that extends [[AbstractMessageSourceFactory]] and the target [[IMessageSourceFactory]] subinterface
        val factoryBytes = classFile.build(thisClass) { classBuilder ->
            classBuilder.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
            classBuilder.withSuperclass(CD_AbstractMessageSourceFactory)
            classBuilder.withInterfaceSymbols(ClassDesc.of(sourceFactoryType.jvmName))

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

        return MethodHandles.lookup().defineClass(factoryBytes)
            .declaredConstructors.single()
            .newInstance(params) as IMessageSourceFactory<*>
    }
}

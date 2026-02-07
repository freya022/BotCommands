package dev.freya02.botcommands.jda.keepalive.internal.transformer

import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.classfile.*
import java.lang.constant.ConstantDescs.CD_String
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.MethodTypeDesc

private val logger = KotlinLogging.logger { }

internal object JDAServiceTransformer : AbstractClassFileTransformer("io/github/freya022/botcommands/api/core/JDAService") {

    override fun transform(classData: ByteArray): ByteArray {
        val classFile = ClassFile.of()
        val classModel = classFile.parse(classData)

        return classFile.transformClass(
            classModel,
            WrapOnReadyEventWithJDABuilderSessionTransform(classModel)
        )
    }
}

private class WrapOnReadyEventWithJDABuilderSessionTransform(private val classModel: ClassModel) : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        // Put the original code of onReadyEvent in the lambda,
        // it will be fired by JDABuilderSession.withBuilderSession in onReadyEvent
        val targetMethod = classModel.findMethod(TARGET_NAME, TARGET_SIGNATURE)

        logger.trace { "Moving ${targetMethod.toFullyQualifiedString()} to '$NEW_NAME'" }
        targetMethod.transferCodeTo(NEW_NAME)
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.matches(TARGET_NAME, TARGET_SIGNATURE)) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to wrap the code in a build session" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val readyEventSlot = codeBuilder.parameterSlot(0)
                val eventManagerSlot = codeBuilder.parameterSlot(1)

                val contextSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val sessionKeySlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val sessionRunnableSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // var context = event.getContext()
                // We could inline this to avoid a successive store/load,
                // but I think using variables is probably a better practice, let's leave the optimization to the VM
                codeBuilder.aload(readyEventSlot)
                codeBuilder.invokevirtual(CD_BReadyEvent, "getContext", MethodTypeDesc.of(CD_BContext))
                codeBuilder.astore(contextSlot)

                // var key = JDABuilderSession.getCacheKey(context)
                codeBuilder.aload(contextSlot)
                codeBuilder.invokestatic(CD_JDABuilderSession, "getCacheKey", MethodTypeDesc.of(CD_String, CD_BContext))
                codeBuilder.astore(sessionKeySlot)

                // THE KEY IS NULLABLE
                // If it is, then don't make a session
                val nullKeyLabel = codeBuilder.newLabel()

                // if (key == null) -> nullKeyLabel
                codeBuilder.aload(sessionKeySlot)
                codeBuilder.ifnull(nullKeyLabel)

                // Runnable sessionRunnable = () -> [lambdaName](event, eventManager)
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(readyEventSlot)
                codeBuilder.aload(eventManagerSlot)
                codeBuilder.invokedynamic(
                    createLambda(
                        interfaceMethod = Runnable::run,
                        targetType = CD_JDAService,
                        targetMethod = NEW_NAME,
                        targetMethodReturnType = CD_void,
                        targetMethodArguments = listOf(),
                        capturedTypes = listOf(CD_BReadyEvent, CD_IEventManager),
                        isStatic = false
                    )
                )
                codeBuilder.astore(sessionRunnableSlot)

                // JDABuilderSession.withBuilderSession(key, sessionRunnable)
                codeBuilder.aload(sessionKeySlot)
                codeBuilder.aload(sessionRunnableSlot)
                codeBuilder.invokestatic(CD_JDABuilderSession, "withBuilderSession", MethodTypeDesc.of(CD_void, CD_String, CD_Runnable))

                // Required
                codeBuilder.return_()

                // nullKeyLabel code
                codeBuilder.labelBinding(nullKeyLabel)
                codeBuilder.return_()
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "onReadyEvent$${InternalModuleNames.CORE}"
        const val TARGET_SIGNATURE = "(Lio/github/freya022/botcommands/api/core/events/BReadyEvent;Lnet/dv8tion/jda/api/hooks/IEventManager;)V"

        const val NEW_NAME = $$"lambda$onReadyEvent$$${InternalModuleNames.CORE}$withBuilderSession"
    }
}

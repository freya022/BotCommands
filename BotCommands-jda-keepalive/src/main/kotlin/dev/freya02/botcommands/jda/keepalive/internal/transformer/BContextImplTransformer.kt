package dev.freya02.botcommands.jda.keepalive.internal.transformer

import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.classfile.*
import java.lang.constant.ConstantDescs.CD_String
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.AccessFlag

private val logger = KotlinLogging.logger { }

internal object BContextImplTransformer : AbstractClassFileTransformer("io/github/freya022/botcommands/internal/core/BContextImpl") {

    override fun transform(classData: ByteArray): ByteArray {
        val classFile = ClassFile.of()
        val classModel = classFile.parse(classData)

        return classFile.transformClass(
            classModel,
            DeferScheduleShutdownSignalTransform(classModel)
        )
    }
}

private class DeferScheduleShutdownSignalTransform(private val classModel: ClassModel) : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        val targetMethod = classModel.findMethod(TARGET_NAME, TARGET_SIGNATURE)
        logger.trace { "Moving ${targetMethod.toFullyQualifiedString()} to '$NEW_NAME'" }
        targetMethod.transferCodeTo(NEW_NAME)
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.matches(TARGET_NAME, TARGET_SIGNATURE)) return classBuilder.retain(classElement)

        logger.trace { "Transforming BContextImpl#${TARGET_NAME}${TARGET_SIGNATURE} to defer shutdown signal scheduling" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withFlags(methodModel.flags().flagsMask().withVisibility(AccessFlag.PUBLIC))

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val afterShutdownSignalSlot = codeBuilder.parameterSlot(0)
                val doScheduleShutdownSignalSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val sessionKeySlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val builderSessionSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // Runnable doScheduleShutdownSignal = () -> this.doScheduleShutdownSignal(afterShutdownSignal)
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(afterShutdownSignalSlot)
                codeBuilder.invokedynamic(
                    createLambda(
                        interfaceMethod = Runnable::run,
                        targetType = CD_BContextImpl,
                        targetMethod = NEW_NAME,
                        targetMethodReturnType = CD_void,
                        targetMethodArguments = listOf(),
                        capturedTypes = listOf(CD_Function0),
                        isStatic = false
                    )
                )
                codeBuilder.astore(doScheduleShutdownSignalSlot)

                // String sessionKey = JDABuilderSession.getCacheKey(this)
                codeBuilder.aload(thisSlot)
                codeBuilder.invokestatic(CD_JDABuilderSession, "getCacheKey", MethodTypeDesc.of(CD_String, CD_BContext))
                codeBuilder.astore(sessionKeySlot)

                // JDABuilderSession builderSession = JDABuilderSession.getSession(sessionKey)
                codeBuilder.aload(sessionKeySlot)
                codeBuilder.invokestatic(CD_JDABuilderSession, "getSession", MethodTypeDesc.of(CD_JDABuilderSession, CD_String))
                codeBuilder.astore(builderSessionSlot)

                // builderSession.onScheduleShutdownSignal(doScheduleShutdownSignal, afterShutdownSignal)
                codeBuilder.aload(builderSessionSlot)
                codeBuilder.aload(doScheduleShutdownSignalSlot)
                codeBuilder.aload(afterShutdownSignalSlot)
                codeBuilder.invokevirtual(CD_JDABuilderSession, "onScheduleShutdownSignal", MethodTypeDesc.of(CD_void, CD_Runnable, CD_Function0))

                // Required
                codeBuilder.return_()
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "scheduleShutdownSignal"
        const val TARGET_SIGNATURE = "(Lnet/dv8tion/jda/api/JDA;)V"

        const val NEW_NAME = "doScheduleShutdownSignal"
    }
}

package dev.freya02.botcommands.jda.keepalive.internal.transformer

import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.classfile.*
import java.lang.classfile.ClassFile.*
import java.lang.classfile.instruction.InvokeInstruction
import java.lang.constant.ConstantDescs.CD_String
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.MethodTypeDesc

private val logger = KotlinLogging.logger { }

internal object JDAImplTransformer : AbstractClassFileTransformer("net/dv8tion/jda/internal/JDAImpl") {

    override fun transform(classData: ByteArray): ByteArray {
        val classFile = ClassFile.of()
        val classModel = classFile.parse(classData)
        return classFile.transformClass(
            classModel,
            CaptureSessionKeyTransform()
                .andThen(DeferShutdownTransform(classModel))
                .andThen(DeferShutdownNowTransform(classModel))
                .andThen(AwaitShutdownTransform())
        )
    }
}

private class CaptureSessionKeyTransform : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        logger.trace { "Adding JDAImpl#${CACHE_KEY_NAME}" }
        classBuilder.withField(CACHE_KEY_NAME, CD_String, ACC_PRIVATE or ACC_FINAL)

        logger.trace { "Adding JDAImpl#getBuilderSession()" }
        classBuilder.withMethod("getBuilderSession", MethodTypeDesc.of(CD_JDABuilderSession), ACC_PUBLIC) { methodBuilder ->
            methodBuilder.withCode { codeBuilder ->
                codeBuilder.aload(codeBuilder.receiverSlot())
                codeBuilder.getfield(CD_JDAImpl, CACHE_KEY_NAME, CD_String)
                codeBuilder.invokestatic(CD_JDABuilderSession, "getSession", MethodTypeDesc.of(CD_JDABuilderSession, CD_String))
                codeBuilder.areturn()
            }
        }
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        // No need to check the signature, we can assign the field in all constructors
        if (!methodModel.methodName().equalsString("<init>")) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to store the session key" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            val codeModel = methodElement as? CodeModel ?: return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                // this.cacheKey = JDABuilderSession.currentSession().getKey()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokestatic(CD_JDABuilderSession, "currentSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.invokevirtual(CD_JDABuilderSession, "getKey", MethodTypeDesc.of(CD_String))
                codeBuilder.putfield(CD_JDAImpl, CACHE_KEY_NAME, CD_String)

                // Add existing instructions
                codeModel.forEach { codeBuilder.with(it) }
            }
        }
    }

    private companion object {
        const val CACHE_KEY_NAME = "cacheKey"
    }
}

private class DeferShutdownTransform(private val classModel: ClassModel) : ContextualClassTransform {

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

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to defer execution" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val doShutdownSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val builderSessionSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // Runnable doShutdown = this::doShutdown
                codeBuilder.aload(thisSlot)
                codeBuilder.invokedynamic(
                    createLambda(
                        interfaceMethod = Runnable::run,
                        targetType = CD_JDAImpl,
                        targetMethod = NEW_NAME,
                        targetMethodReturnType = CD_void,
                        targetMethodArguments = listOf(),
                        capturedTypes = listOf(),
                        isStatic = false
                    )
                )
                codeBuilder.astore(doShutdownSlot)

                // var builderSession = getBuilderSession()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokevirtual(CD_JDAImpl, "getBuilderSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.astore(builderSessionSlot)

                // builderSession.onShutdown(this, this::doShutdown);
                codeBuilder.aload(builderSessionSlot)
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(doShutdownSlot)
                codeBuilder.invokevirtual(CD_JDABuilderSession, "onShutdown", MethodTypeDesc.of(CD_void, CD_JDA, CD_Runnable))

                codeBuilder.return_()
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "shutdown"
        const val TARGET_SIGNATURE = "()V"

        const val NEW_NAME = "doShutdown"
    }
}

private class DeferShutdownNowTransform(private val classModel: ClassModel) : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        val targetMethod = classModel.findMethod(TARGET_NAME, TARGET_SIGNATURE)

        logger.trace { "Moving ${targetMethod.toFullyQualifiedString()} to $NEW_NAME, replacing shutdown() with doShutdown()" }
        classBuilder.withMethod(
            NEW_NAME,
            MethodTypeDesc.of(CD_void),
            ACC_PRIVATE or ACC_SYNTHETIC or ACC_FINAL
        ) { methodBuilder ->
            val codeModel = targetMethod.code().get()

            methodBuilder.withCode { codeBuilder ->
                // Move the shutdownNow() code to doShutdownNow()
                codeModel.forEach { codeElement ->
                    // Replace shutdown() with doShutdown() so we don't call [[JDABuilderSession#onShutdown]] more than once
                    if (codeElement is InvokeInstruction && codeElement.name().equalsString("shutdown")) {
                        require(codeElement.type().equalsString("()V"))
                        codeBuilder.invokevirtual(codeElement.owner().asSymbol(), "doShutdown", MethodTypeDesc.of(CD_void))
                        return@forEach
                    }

                    codeBuilder.with(codeElement)
                }
            }
        }
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.matches(TARGET_NAME, TARGET_SIGNATURE)) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to defer execution" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val doShutdownNowSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val builderSessionSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // Runnable doShutdownNow = this::doShutdownNow
                codeBuilder.aload(thisSlot)
                codeBuilder.invokedynamic(
                    createLambda(
                        interfaceMethod = Runnable::run,
                        targetType = CD_JDAImpl,
                        targetMethod = NEW_NAME,
                        targetMethodReturnType = CD_void,
                        targetMethodArguments = listOf(),
                        capturedTypes = listOf(),
                        isStatic = false
                    )
                )
                codeBuilder.astore(doShutdownNowSlot)

                // var builderSession = getBuilderSession()
                codeBuilder.aload(thisSlot)
                codeBuilder.invokevirtual(CD_JDAImpl, "getBuilderSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.astore(builderSessionSlot)

                // builderSession.onShutdown(this, this::doShutdownNow);
                codeBuilder.aload(builderSessionSlot)
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(doShutdownNowSlot)
                codeBuilder.invokevirtual(CD_JDABuilderSession, "onShutdown", MethodTypeDesc.of(CD_void, CD_JDA, CD_Runnable))

                codeBuilder.return_()
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "shutdownNow"
        const val TARGET_SIGNATURE = "()V"

        const val NEW_NAME = "doShutdownNow"
    }
}

private class AwaitShutdownTransform : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.methodName().equalsString("awaitShutdown")) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to immediately return" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                codeBuilder.iconst_0()
                codeBuilder.ireturn()
            }
        }
    }
}

package dev.freya02.botcommands.jda.keepalive.internal.transformer

import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderConfiguration
import dev.freya02.botcommands.jda.keepalive.internal.transformer.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.classfile.*
import java.lang.classfile.ClassFile.*
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.AccessFlag
import java.util.function.Supplier

private val logger = KotlinLogging.logger { }

internal object JDABuilderTransformer : AbstractClassFileTransformer("net/dv8tion/jda/api/JDABuilder") {

    override fun transform(classData: ByteArray): ByteArray {
        val classFile = ClassFile.of()
        val classModel = classFile.parse(classData)
        
        return classFile.transformClass(
            classModel,
            CaptureSetterParametersTransform()
                .andThen(CaptureConstructorParametersTransform(classModel))
                .andThen(DeferBuildAndSetBufferingEventManagerTransform(classModel))
        )
    }
}

private class CaptureConstructorParametersTransform(private val classModel: ClassModel) : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        classModel.findMethod(TARGET_NAME, TARGET_SIGNATURE)
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.matches(TARGET_NAME, TARGET_SIGNATURE)) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to capture parameters" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            val codeModel = methodElement as? CodeModel ?: return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val builderConfigurationSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val tokenSlot = codeBuilder.parameterSlot(0)
                val intentsSlot = codeBuilder.parameterSlot(1)

                // JDABuilderConfiguration configuration = JDABuilderSession.currentSession().getConfiguration();
                codeBuilder.invokestatic(CD_JDABuilderSession, "currentSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.invokevirtual(CD_JDABuilderSession, "getConfiguration", MethodTypeDesc.of(CD_JDABuilderConfiguration))
                codeBuilder.astore(builderConfigurationSlot)

                // configuration.onInit(token, intents);
                codeBuilder.aload(builderConfigurationSlot)
                codeBuilder.aload(tokenSlot)
                codeBuilder.iload(intentsSlot)
                codeBuilder.invokevirtual(CD_JDABuilderConfiguration, "onInit", MethodTypeDesc.of(CD_void, CD_String, CD_int))

                // Add existing instructions
                codeModel.forEach { codeBuilder.with(it) }
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "<init>"
        const val TARGET_SIGNATURE = "(Ljava/lang/String;I)V"
    }
}

private class DeferBuildAndSetBufferingEventManagerTransform(private val classModel: ClassModel) : ContextualClassTransform {

    context(classBuilder: ClassBuilder)
    override fun atStartContextual() {
        val targetMethod = classModel.findMethod(TARGET_NAME, TARGET_SIGNATURE)

        logger.trace { "Adding JDABuilder#${NEW_NAME}() to set an event manager and build" }
        classBuilder.withMethod(
            NEW_NAME,
            MethodTypeDesc.of(CD_JDA),
            ACC_PRIVATE or ACC_SYNTHETIC or ACC_FINAL
        ) { methodBuilder ->
            val codeModel = targetMethod.code().get()

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val bufferingEventManagerSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // JDABuilder's eventManager is null by default,
                // however, the framework mandates setting a framework-provided event manager,
                // so let's just throw if it is null.
                val nullEventManagerLabel = codeBuilder.newLabel()
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(CD_JDABuilder, "eventManager", CD_IEventManager)
                codeBuilder.ifnull(nullEventManagerLabel)

                // var bufferingEventManager = new BufferingEventManager
                codeBuilder.new_(CD_BufferingEventManager)
                codeBuilder.astore(bufferingEventManagerSlot)

                // bufferingEventManager.<init>(eventManager)
                codeBuilder.aload(bufferingEventManagerSlot)
                codeBuilder.aload(thisSlot)
                codeBuilder.getfield(CD_JDABuilder, "eventManager", CD_IEventManager)
                codeBuilder.invokespecial(CD_BufferingEventManager, "<init>", MethodTypeDesc.of(CD_void, CD_IEventManager))

                // this.setEventManager(eventManager)
                codeBuilder.aload(thisSlot)
                codeBuilder.aload(bufferingEventManagerSlot)
                codeBuilder.invokevirtual(CD_JDABuilder, "setEventManager", MethodTypeDesc.of(CD_JDABuilder, CD_IEventManager))

                // Move the build() code to doBuild()
                codeModel.forEach { codeBuilder.with(it) }

                // Branch when "eventManager" is null
                codeBuilder.labelBinding(nullEventManagerLabel)

                codeBuilder.new_(CD_IllegalStateException)
                codeBuilder.dup()
                codeBuilder.ldc("The event manager must be set using the one provided in JDAService#createJDA")
                codeBuilder.invokespecial(CD_IllegalStateException, "<init>", MethodTypeDesc.of(CD_void, CD_String))
                codeBuilder.athrow()
            }
        }
    }

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.matches(TARGET_NAME, TARGET_SIGNATURE)) return classBuilder.retain(classElement)

        logger.trace { "Transforming ${methodModel.toFullyQualifiedString()} to defer calls" }
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            if (methodElement !is CodeModel) return@transformMethod methodBuilder.retain(methodElement)

            methodBuilder.withCode { codeBuilder ->
                val thisSlot = codeBuilder.receiverSlot()

                val builderSessionSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val doBuildSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
                val jdaSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // Supplier<JDA> doBuild = this::doBuild
                codeBuilder.aload(thisSlot)

                codeBuilder.invokedynamic(
                    createLambda(
                        interfaceMethod = Supplier<*>::get,
                        targetType = CD_JDABuilder,
                        targetMethod = NEW_NAME,
                        targetMethodReturnType = CD_JDA,
                        targetMethodArguments = listOf(),
                        capturedTypes = emptyList(),
                        isStatic = false
                    )
                )
                codeBuilder.astore(doBuildSlot)

                // JDABuilderSession session = JDABuilderSession.currentSession();
                codeBuilder.invokestatic(CD_JDABuilderSession, "currentSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.astore(builderSessionSlot)

                // var jda = session.onBuild(this::doBuild);
                codeBuilder.aload(builderSessionSlot)
                codeBuilder.aload(doBuildSlot)
                codeBuilder.invokevirtual(CD_JDABuilderSession, "onBuild", MethodTypeDesc.of(CD_JDA, CD_Supplier))
                // Again, prefer using a variable for clarity
                codeBuilder.astore(jdaSlot)

                codeBuilder.aload(jdaSlot)
                codeBuilder.areturn()
            }
        }
    }

    private companion object {
        const val TARGET_NAME = "build"
        const val TARGET_SIGNATURE = "()Lnet/dv8tion/jda/api/JDA;"

        const val NEW_NAME = "doBuild"
    }
}

private class CaptureSetterParametersTransform : ContextualClassTransform {

    private val builderSessionMethods: Set<MethodDesc> = ClassFile.of()
        .parse(JDABuilderConfiguration::class.java.getResourceAsStream("JDABuilderConfiguration.class")!!.readAllBytes())
        .methods()
        .mapTo(hashSetOf(), ::MethodDesc)

    context(classBuilder: ClassBuilder)
    override fun acceptContextual(classElement: ClassElement) {
        val methodModel = classElement as? MethodModel ?: return classBuilder.retain(classElement)
        if (!methodModel.flags().has(AccessFlag.PUBLIC)) return classBuilder.retain(classElement)
        if (methodModel.flags().has(AccessFlag.STATIC)) return classBuilder.retain(classElement)
        if (methodModel.methodName().stringValue() == "build") return classBuilder.retain(classElement)

        // Log is done later
        classBuilder.transformMethod(methodModel) { methodBuilder, methodElement ->
            val codeModel = methodElement as? CodeModel ?: return@transformMethod methodBuilder.retain(methodElement)

            val hasBuilderSessionMethod = methodModel.let(::MethodDesc) in builderSessionMethods
            methodBuilder.withCode { codeBuilder ->
                val builderConfigurationSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

                // JDABuilderConfiguration configuration = JDABuilderSession.currentSession().getConfiguration();
                codeBuilder.invokestatic(CD_JDABuilderSession, "currentSession", MethodTypeDesc.of(CD_JDABuilderSession))
                codeBuilder.invokevirtual(CD_JDABuilderSession, "getConfiguration", MethodTypeDesc.of(CD_JDABuilderConfiguration))
                codeBuilder.astore(builderConfigurationSlot)

                val methodName = methodModel.methodName().stringValue()
                if (hasBuilderSessionMethod) {
                    logger.trace { "Registering ${methodModel.toFullyQualifiedString()} as a cache-compatible method" }

                    // Set return type to "void" because our method won't return JDABuilder, and it doesn't matter anyway
                    val methodType = methodModel.methodTypeSymbol().changeReturnType(CD_void)

                    // configuration.theMethod(parameters);
                    codeBuilder.aload(builderConfigurationSlot)
                    methodType.parameterList().forEachIndexed { index, parameter ->
                        val typeKind = TypeKind.fromDescriptor(parameter.descriptorString())
                        val slot = codeBuilder.parameterSlot(index)
                        codeBuilder.loadLocal(typeKind, slot)
                    }
                    codeBuilder.invokevirtual(CD_JDABuilderConfiguration, methodName, methodType)
                } else {
                    logger.trace { "Skipping ${methodModel.toFullyQualifiedString()} as it does not have an equivalent method handler" }

                    val signature = methodName + "(${methodModel.methodTypeSymbol().parameterList().joinToString { it.displayName() }})"

                    // configuration.markUnsupportedValue()
                    codeBuilder.aload(builderConfigurationSlot)
                    codeBuilder.ldc(signature)
                    codeBuilder.invokevirtual(CD_JDABuilderConfiguration, "markUnsupportedValue", MethodTypeDesc.of(CD_void, CD_String))
                }

                // Add existing instructions
                codeModel.forEach { codeBuilder.with(it) }
            }
        }
    }

    // Utility to match methods using their name and parameters, but not return type
    private data class MethodDesc(
        val name: String,
        val paramTypes: List<ClassDesc>
    ) {
        constructor(methodModel: MethodModel) : this(
            methodModel.methodName().stringValue(),
            methodModel.methodTypeSymbol().parameterList(),
        )
    }
}

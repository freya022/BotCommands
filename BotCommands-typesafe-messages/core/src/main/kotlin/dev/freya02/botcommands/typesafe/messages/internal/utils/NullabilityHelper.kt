package dev.freya02.botcommands.typesafe.messages.internal.utils

import dev.freya02.botcommands.typesafe.messages.internal.codegen.utils.toClassDesc
import dev.freya02.botcommands.typesafe.messages.internal.exceptions.throwInternal
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.oshai.kotlinlogging.KLogger
import java.io.InputStream
import java.lang.classfile.*
import java.lang.classfile.Annotation
import java.lang.constant.MethodTypeDesc
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmName

internal object NullabilityHelper {
    /** Index of Java parameters, 0 is first argument, not instance parameter */
    private typealias FormalParamIndex = Int

    internal fun loadNullableParameterIndexesFromKtReflect(function: KFunction<*>): Set<FormalParamIndex> = buildSet {
        for ((index, parameter) in function.valueParameters.withIndex()) {
            if (parameter.annotations.any { it.annotationClass.simpleName == "Nullable" })
                add(index)
            else if (parameter.type.isMarkedNullable)
                add(index)
            else if (parameter.type.annotations.any { it.annotationClass.simpleName == "Nullable" })
                add(index)
        }
    }

    internal fun getNullableParameterIndexes(logger: KLogger, declaringClass: KClass<*>, function: KFunction<*>): Set<FormalParamIndex> {
        val classModel = try {
            val bytes = declaringClass.java.classLoader
                .getResourceAsStream(declaringClass.java.name.replace('.', '/') + ".class")
                ?.use(InputStream::readAllBytes)
            if (bytes == null) {
                logger.warn { "Binary of '${declaringClass.jvmName}' was not found" }
                return loadNullableParameterIndexesFromKtReflect(function)
            }
            ClassFile.of().parse(bytes)
        } catch (e: Exception) {
            logger.warn(e) { "Unable to load binary of '${declaringClass.jvmName}'" }
            return loadNullableParameterIndexesFromKtReflect(function)
        }

        val methodModel = findMethodModel(classModel, function)
        return buildSet {
            // Check parameter annotations
            fun List<List<Annotation>>.collectNullables() {
                forEachIndexed { paramIndex, annotations ->
                    if (annotations.any { it.className().endsWith("Nullable;") }) {
                        add(paramIndex)
                    }
                }
            }

            methodModel.findAttribute(Attributes.runtimeInvisibleParameterAnnotations()).getOrNull()?.parameterAnnotations()?.collectNullables()
            methodModel.findAttribute(Attributes.runtimeVisibleParameterAnnotations()).getOrNull()?.parameterAnnotations()?.collectNullables()

            // Check annotations of type parameters present in parameters
            fun List<TypeAnnotation>.collectNullables() {
                for (typeAnnotation in this) {
                    val targetInfo = typeAnnotation.targetInfo() as? TypeAnnotation.FormalParameterTarget
                        ?: continue
                    if (typeAnnotation.annotation().className().endsWith("Nullable;")) {
                        add(targetInfo.formalParameterIndex())
                    }
                }
            }

            methodModel.findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).getOrNull()?.annotations()?.collectNullables()
            methodModel.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).getOrNull()?.annotations()?.collectNullables()
        }
    }

    private fun findMethodModel(classModel: ClassModel, function: KFunction<*>): MethodModel {
        val javaMethod = function.javaMethod!!
        val functionsWithSameName = classModel.methods().filter { it.methodName().equalsString(javaMethod.name) }
        return if (functionsWithSameName.isEmpty()) {
            throwInternal("Could not locate method ${function.getSignature(qualifiedClass = true, source = false)} ")
        } else if (functionsWithSameName.size == 1) {
            functionsWithSameName.first()
        } else { // Overloads
            val expectedMethodType = MethodTypeDesc.of(
                javaMethod.returnType.toClassDesc(),
                javaMethod.parameterTypes.map { it.toClassDesc() },
            )

            functionsWithSameName.singleOrNull { it.methodTypeSymbol() == expectedMethodType }
                ?: throwInternal("Could not locate method ${function.getSignature(qualifiedClass = true, source = false)}")
        }
    }
}

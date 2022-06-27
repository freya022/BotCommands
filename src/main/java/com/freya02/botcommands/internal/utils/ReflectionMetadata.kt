package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.Optional
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.asKFunction
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation

internal object ReflectionMetadata {
    internal class KFunctionMetadata(val function: KFunction<*>, val isJava: Boolean)

    internal class KParameterMetadata(
        annotationMap: Map<KClass<*>, Annotation>,
        val isNullable: Boolean,
        val isJava: Boolean,
        val function: KFunction<*>
    ) {
        val annotationMap: Map<KClass<*>, Annotation> = Collections.unmodifiableMap(annotationMap)
    }

    private var scannedParams: Boolean = false

    private val paramMetadataMap_: MutableMap<KParameter, KParameterMetadata> = hashMapOf()
    private val paramMetadataMap: Map<KParameter, KParameterMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access a KParameter metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(paramMetadataMap_)
    }

    private val functionMetadataMap_: MutableMap<KFunction<*>, KFunctionMetadata> = hashMapOf()
    private val functionMetadataMap: Map<KFunction<*>, KFunctionMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access a function metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(functionMetadataMap_)
    }

    internal fun runScan(packages: Collection<String>, userClasses: Collection<Class<*>>): ScanResult = ClassGraph()
        .acceptPackages(*packages.toTypedArray())
        .acceptClasses(*userClasses.map { it.simpleName }.toTypedArray())
        .enableMethodInfo()
        .enableAnnotationInfo()
        .scan()

    internal fun readAnnotations(scanResult: ScanResult) {
        for (classInfo in scanResult.allClasses) {
            val isJavaParameter = !classInfo.hasAnnotation("kotlin.Metadata")

            for (methodInfo in classInfo.declaredMethodInfo) {
                val kFunction = methodInfo.loadClassAndGetMethod().asKFunction()
                val parameters = kFunction.nonInstanceParameters
                for ((j, parameterInfo) in methodInfo.parameterInfo.dropLast(if (kFunction.isSuspend) 1 else 0).withIndex()) {
                    val parameter = parameters[j]

                    val annotationMap: MutableMap<KClass<*>, Annotation> = hashMapOf()

                    for (annotationInfo in parameterInfo.annotationInfo) {
                        @Suppress("UNCHECKED_CAST")
                        annotationMap[annotationInfo.classInfo.loadClass().kotlin] =
                            parameter.findAnnotations(annotationInfo.classInfo.loadClass().kotlin as KClass<out Annotation>)
                                .firstOrNull() ?: annotationInfo.loadClassAndInstantiate()
                    }

                    val isNullableAnnotated =
                        parameterInfo.annotationInfo.any { it.name.endsWith("Nullable") } or parameter.hasAnnotation<Optional>()
                    val isNullableMarked = parameter.type.isMarkedNullable
                    if (!isJavaParameter && isNullableAnnotated && !isNullableMarked) {
                        throwUser("Parameter $parameter is annotated as being nullable/optional but runtime checks from annotations will prevent this from being nullable")
                    }

                    paramMetadataMap_[parameter] =
                        KParameterMetadata(
                            annotationMap,
                            isNullableAnnotated or isNullableMarked,
                            isJavaParameter,
                            kFunction
                        )
                }

                functionMetadataMap_[kFunction] = KFunctionMetadata(kFunction, isJavaParameter)
            }
        }

        scannedParams = true
    }

    internal inline fun <reified A : Annotation> KParameter.hasAnnotation_(): Boolean {
        return (paramMetadataMap[this]
            ?: throwUser("Tried to access a KParameter which hasn't been scanned: $this, the parameter must be accessible and in the search path")).annotationMap[A::class] != null
    }

    internal inline fun <reified A : Annotation> KParameter.findAnnotation_(): A? {
        return (paramMetadataMap[this]
            ?: throwUser("Tried to access a KParameter which hasn't been scanned: $this, the parameter must be accessible and in the search path")).annotationMap[A::class] as? A
    }

    internal val KParameter.isNullable: Boolean
        get() = (paramMetadataMap[this]
            ?: throwUser("Tried to access a KParameter which hasn't been scanned: $this, the parameter must be accessible and in the search path")).isNullable

    internal val KParameter.function
        get() = (paramMetadataMap[this]
            ?: throwUser("Tried to access a KParameter which hasn't been scanned: $this, the parameter must be accessible and in the search path")).function

    internal val KParameter.isJava
        get() = (paramMetadataMap[this]
            ?: throwUser("Tried to access a KParameter which hasn't been scanned: $this, the parameter must be accessible and in the search path")).isJava

    internal val KFunction<*>.isJava
        get() = (functionMetadataMap[this]
            ?: throwUser("Tried to access a KFunction which hasn't been scanned: $this, the function must be accessible and in the search path")).isJava
}
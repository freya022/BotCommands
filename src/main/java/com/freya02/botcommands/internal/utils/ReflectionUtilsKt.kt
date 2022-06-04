package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.ConditionalUse
import com.freya02.botcommands.annotations.api.annotations.Optional
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.*
import io.github.classgraph.ClassGraph
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

private val LOGGER = Logging.getLogger()

internal class KFunctionMetadata(val function: KFunction<*>, val isJava: Boolean)

internal class KParameterMetadata(
    annotationMap: Map<KClass<*>, Annotation>,
    val isNullable: Boolean,
    val isJava: Boolean,
    val function: KFunction<*>
) {
    val annotationMap: Map<KClass<*>, Annotation> = Collections.unmodifiableMap(annotationMap)
}

internal object ReflectionUtilsKt {
    private var scannedParams: Boolean = false

    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

    private val _paramMetadataMap: MutableMap<KParameter, KParameterMetadata> = hashMapOf()
    internal val paramMetadataMap: Map<KParameter, KParameterMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access a KParameter metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(_paramMetadataMap)
    }

    private val _functionMetadataMap: MutableMap<KFunction<*>, KFunctionMetadata> = hashMapOf()
    internal val functionMetadataMap: Map<KFunction<*>, KFunctionMetadata> by lazy {
        if (!scannedParams)
            throwInternal("Tried to access a function metadata but they haven't been scanned yet")

        Collections.unmodifiableMap(_functionMetadataMap)
    }

    internal fun Method.asKFunction(): KFunction<*> {
        return this.kotlinFunction ?: throwInternal("Unable to get kotlin function from $this")
    }

    internal fun KFunction<*>.reflectReference(): KFunction<*> {
        if (this.isStatic) {
            throwUser(this, "Function must not be static")
        }

        return reflectedMap.computeIfAbsent(this) {
            return@computeIfAbsent when (this) { //Try to match the original function
                is CallableReference -> {
                    (owner as KClass<*>).declaredMemberFunctions.find {//Don't use bound receiver, might be null somehow
                        it.name == name
                                && it.nonInstanceParameters.zip(nonInstanceParameters).all { param ->
                            param.first.name == param.second.name
                                    && param.first.type == param.second.type
                        }
                    } ?: throwInternal("Unable to reflect function reference: $this")
                }
                else -> this
            }
        }
    }

    // TODO Transform this to also use CG to scan package classes
    internal fun scanAnnotations(classes: Collection<KClass<*>>) {
        if (classes.isEmpty()) return
        val result = ClassGraph()
            .acceptClasses(*classes.mapNotNull { it.qualifiedName }.toTypedArray())
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()

        for (classInfo in result.allClasses) {
            val isJavaParameter = !classInfo.hasAnnotation("kotlin.Metadata")

            for (methodInfo in classInfo.declaredMethodInfo) {
                val kFunction = methodInfo.loadClassAndGetMethod().asKFunction()
                val parameters = kFunction.nonInstanceParameters
                for ((j, parameterInfo) in methodInfo.parameterInfo.withIndex()) {
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

                    _paramMetadataMap[parameter] =
                        KParameterMetadata(annotationMap, isNullableAnnotated or isNullableMarked, isJavaParameter, kFunction)
                }

                _functionMetadataMap[kFunction] = KFunctionMetadata(kFunction, isJavaParameter)
            }
        }

        scannedParams = true
    }

    //TODO use api
    internal inline fun <reified A : Annotation> KParameter.hasAnnotation_(): Boolean {
        return (paramMetadataMap[this]
            ?: throwInternal("Tried to access a KParameter which hasn't been scanned: $this")).annotationMap[A::class] != null
    }

    internal inline fun <reified A : Annotation> KParameter.findAnnotation_(): A? {
        return (paramMetadataMap[this]
            ?: throwInternal("Tried to access a KParameter which hasn't been scanned: $this")).annotationMap[A::class] as? A
    }

    internal val KParameter.isNullable: Boolean
        get() = (paramMetadataMap[this]
            ?: throwInternal("Tried to access a KParameter which hasn't been scanned: $this")).isNullable

    internal val KParameter.function
        get() = (paramMetadataMap[this]
            ?: throwInternal("Tried to access a KParameter which hasn't been scanned: $this")).function

    internal val KParameter.isJava
        get() = (paramMetadataMap[this]
            ?: throwInternal("Tried to access a KParameter which hasn't been scanned: $this")).isJava

    internal val KFunction<*>.isJava
        get() = (functionMetadataMap[this]
            ?: throwInternal("Tried to access a KFunction which hasn't been scanned: $this")).isJava

    internal val KFunction<*>.nonInstanceParameters
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun isInstantiable(clazz: KClass<*>): Boolean {
        var canInstantiate = true
        for (function in clazz.memberFunctions) {
            if (function.hasAnnotation<ConditionalUse>()) {
                if (function.isStatic) {
                    if (function.valueParameters.isEmpty() && function.returnType.jvmErasure == Boolean::class) {
                        requireUser(function.isPublic, function) { "Method must be public" }
                        canInstantiate = function.call() as Boolean
                    } else {
                        LOGGER.warn(
                            "Method {}#{} is annotated @ConditionalUse but does not have the correct signature (return boolean, no parameters)",
                            clazz.simpleName,
                            function.name
                        )
                    }
                } else {
                    LOGGER.warn(
                        "Method {}#{} is annotated @ConditionalUse but is not static",
                        clazz.simpleName,
                        function.name
                    )
                }

                break
            }
        }

        return canInstantiate
    }
}
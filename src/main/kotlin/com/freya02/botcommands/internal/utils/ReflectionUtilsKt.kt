package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.annotations.ConditionalUse
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata.lineNumber
import com.freya02.botcommands.internal.utils.ReflectionMetadata.sourceFile
import io.github.classgraph.ClassInfo
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

private val LOGGER = Logging.getLogger()

internal object ReflectionUtilsKt {
    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

    private val serviceAnnotations: List<KClass<out Annotation>> = listOf(BService::class, ConditionalService::class, InjectedService::class)
    private val loadableServiceAnnotations: List<KClass<out Annotation>> = listOf(BService::class, ConditionalService::class)
    private val serviceAnnotationNames: List<String> = serviceAnnotations.map { it.java.name }

    internal fun Method.asKFunction(): KFunction<*> {
        return this.kotlinFunction ?: throwInternal("Unable to get kotlin function from $this")
    }

    internal fun KFunction<*>.reflectReference(): KFunction<*> {
        if (this.isStatic) {
            throwUser(this, "Function must not be static")
        }

        synchronized(reflectedMap) {
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
    }

    internal val KFunction<*>.nonInstanceParameters
        get() = parameters.filter { it.kind != KParameter.Kind.INSTANCE }

    internal val KFunction<*>.shortSignatureNoSrc: String
        get() {
            val declaringClassName = this.javaMethod?.declaringClass?.simpleName ?: "<no-java-method>"
            val methodName = this.name
            val parameters = this.valueParameters.joinToString { it.type.jvmErasure.java.simpleName }
            return "$declaringClassName#$methodName($parameters)"
        }

    internal val KFunction<*>.shortSignature: String
        get() {
            val returnType = this.returnType.simpleName
            val source = this.javaMethod.let { method ->
                return@let when {
                    method != null && this.lineNumber != 0 -> {
                        val sourceFile = method.declaringClass.sourceFile
                        val lineNumber = this.lineNumber

                        "$sourceFile:$lineNumber"
                    }
                    else -> "<no-source>"
                }
            }
            return "$shortSignatureNoSrc: $returnType ($source)"
        }

    val KProperty<*>.referenceString: String
        get() {
            val callableReference = (this as? CallableReference)
                ?: throwInternal("Referenced field doesn't seem to be compiler generated, exact type: ${this::class}")
            return (callableReference.owner as KClass<*>).java.simpleName + "#" + this.name
        }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun isInstantiable(info: ClassInfo): Boolean {
        var canInstantiate = true
        for (methodInfo in info.methodInfo) {
            if (methodInfo.hasAnnotation(ConditionalUse::class.java)) {
                if (methodInfo.isStatic) {
                    val function = methodInfo.loadClassAndGetMethod().asKFunction()
                    if (function.parameters.isEmpty() && function.returnType.jvmErasure == Boolean::class) {
                        requireUser(function.isPublic, function) { "Method must be public" }
                        canInstantiate = function.call() as Boolean
                    } else {
                        LOGGER.warn(
                            "Method {}#{} is annotated @ConditionalUse but does not have the correct signature (return boolean, no parameters)",
                            info.simpleName,
                            function.name
                        )
                    }
                } else {
                    LOGGER.warn(
                        "Method {}#{} is annotated @ConditionalUse but is not static",
                        info.simpleName,
                        methodInfo.name
                    )
                }

                break
            }
        }

        return canInstantiate
    }

    internal fun ClassInfo.isService() = serviceAnnotationNames.any { this.hasAnnotation(it) }
    internal fun KClass<*>.isService() = serviceAnnotations.any { this.findAnnotations(it).isNotEmpty() }
    internal fun KClass<*>.isLoadableService() = loadableServiceAnnotations.any { this.findAnnotations(it).isNotEmpty() }
}
package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.ConditionalUse
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.*
import io.github.classgraph.ClassInfo
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

private val LOGGER = Logging.getLogger()

internal object ReflectionUtilsKt {
    private val reflectedMap: MutableMap<KFunction<*>, KFunction<*>> = hashMapOf()

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

    internal val KFunction<*>.shortSignature: String
        get() {
            val declaringClassName = this.javaMethod?.declaringClass?.simpleName ?: "<no-java-method>"
            val methodName = this.name
            val parameters = this.valueParameters.joinToString { it.type.jvmErasure.java.simpleName }
            val returnType = this.returnType.simpleName
            return "$declaringClassName#$methodName($parameters): $returnType"
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
}
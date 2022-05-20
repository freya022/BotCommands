package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.ConditionalUse
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.isPublic
import com.freya02.botcommands.internal.isStatic
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwInternal
import io.github.classgraph.ClassGraph
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

private val LOGGER = Logging.getLogger()

object ReflectionUtilsKt {
    private val paramAnnotationsMap: MutableMap<KParameter, MutableMap<KClass<*>, Annotation>> = HashMap()

    fun scanAnnotations(classes: Collection<KClass<*>>) {
        if (classes.isEmpty()) return
        val result = ClassGraph()
            .acceptClasses(*classes.map { it.simpleName }.toTypedArray())
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()

        for (classInfo in result.allClasses) {
            for (methodInfo in classInfo.declaredMethodInfo) {
                val kFunction = methodInfo.loadClassAndGetMethod().kotlinFunction ?: throwInternal("Unable to get kotlin function from ${methodInfo.loadClassAndGetMethod()}")
                val parameters = kFunction.valueParameters
                for ((j, parameterInfo) in methodInfo.parameterInfo.withIndex()) {
                    val parameter = parameters[j]
                    for (annotationInfo in parameterInfo.annotationInfo) {
                        paramAnnotationsMap.computeIfAbsent(parameter) { HashMap() }[annotationInfo.classInfo.loadClass().kotlin] = annotationInfo.loadClassAndInstantiate()
                    }
                }
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    fun isInstantiable(clazz: KClass<*>): Boolean {
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
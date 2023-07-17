package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.commands.annotations.BotPermissions
import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.api.core.utils.enumSetOf
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations

internal object AnnotationUtils {
    //List order is from deepest to most effective
    //aka class --> method
    private fun <A : Annotation> getEffectiveAnnotations(function: KFunction<*>, annotation: KClass<A>): List<A> {
        return function.findAnnotations(annotation) + function.declaringClass.findAnnotations(annotation)
    }

    internal fun getEffectiveTestGuildIds(context: BContextImpl, function: KFunction<*>): TLongSet {
        val testIds: TLongSet = TLongHashSet(context.applicationConfig.testGuildIds)
        val effectiveAnnotations = getEffectiveAnnotations(function, Test::class)
        for (test in effectiveAnnotations) {
            val ids: LongArray = test.guildIds

            if (test.append) {
                testIds.addAll(ids)
            } else {
                testIds.clear()
                testIds.addAll(ids)

                return testIds
            }
        }

        return testIds
    }

    internal fun <A : Annotation> getEffectiveAnnotation(function: KFunction<*>, annotationType: KClass<A>): A? {
        val methodAnnot = function.findAnnotations(annotationType)

        return when {
            methodAnnot.isNotEmpty() -> methodAnnot.first()
            else -> function.declaringClass.findAnnotations(annotationType).firstOrNull()
        }
    }

    internal fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        val annotation = func.findAnnotation<UserPermissions>() ?: return enumSetOf()

        return enumSetOf<Permission>().also { set ->
            set += annotation.value

            if (!annotation.append) {
                return set
            }

            func.declaringClass.findAnnotation<UserPermissions>()?.value?.let {
                set += it
            }
        }
    }

    internal fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        val annotation = func.findAnnotation<BotPermissions>() ?: return enumSetOf()

        return enumSetOf<Permission>().also { set ->
            set += annotation.value

            if (!annotation.append) {
                return set
            }

            func.declaringClass.findAnnotation<BotPermissions>()?.value?.let {
                set += it
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T, A : Annotation> getAnnotationValue(annotation: A, methodName: String): T {
        val kFunction = annotation.annotationClass.declaredMemberProperties.find { it.name == methodName }
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}'")
        return kFunction.call(annotation) as? T
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}' as the type is incorrect, annotation type: ${kFunction.returnType.simpleNestedName}")
    }
}
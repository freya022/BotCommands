package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.api.annotations.AppendMode
import com.freya02.botcommands.api.commands.annotations.BotPermissions
import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.simpleName
import com.freya02.botcommands.internal.throwInternal
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.javaMethod

internal object AnnotationUtils {
    //List order is from deepest to most effective
    //aka class --> method
    private fun <A : Annotation> getEffectiveAnnotations(method: KFunction<*>, annotation: KClass<A>): List<A> {
        return method.findAnnotations(annotation) + method.javaMethod!!.declaringClass.getDeclaredAnnotationsByType(annotation.java)
    }

    fun getEffectiveTestGuildIds(context: BContextImpl, method: KFunction<*>): TLongSet {
        val testIds: TLongSet = TLongHashSet(context.config.applicationConfig.testGuildIds)
        val effectiveAnnotations = getEffectiveAnnotations(method, Test::class)
        for (test in effectiveAnnotations) {
            val ids: LongArray = test.guildIds
            val mode: AppendMode = test.mode

            if (mode == AppendMode.SET) {
                testIds.clear()
                testIds.addAll(ids)

                return testIds
            } else if (mode == AppendMode.ADD) {
                testIds.addAll(ids)
            }
        }

        return testIds
    }

    @JvmStatic
    fun <A : Annotation> getEffectiveAnnotation(method: KFunction<*>, annotationType: KClass<A>): A? {
        val methodAnnot = method.findAnnotations(annotationType)

        return when {
            methodAnnot.isNotEmpty() -> methodAnnot.first()
            else -> method.javaMethod!!.declaringClass.kotlin.findAnnotations(annotationType).firstOrNull()
        }
    }

    fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        val annotation = func.findAnnotation<UserPermissions>() ?: return enumSetOf()

        return enumSetOf<Permission>().also { set ->
            set += annotation.value

            if (annotation.mode == AppendMode.SET) {
                return set
            }

            set += func.javaMethod!!.declaringClass.getAnnotation(UserPermissions::class.java).value
        }
    }

    fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        val annotation = func.findAnnotation<BotPermissions>() ?: return enumSetOf()

        return enumSetOf<Permission>().also { set ->
            set += annotation.value

            if (annotation.mode == AppendMode.SET) {
                return set
            }

            set += func.javaMethod!!.declaringClass.getAnnotation(BotPermissions::class.java).value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, A : Annotation> getAnnotationValue(annotation: A, methodName: String): T {
        val kFunction = annotation.annotationClass.declaredMemberProperties.find { it.name == methodName }
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}'")
        return kFunction.call(annotation) as? T
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}' as the type is incorrect, annotation type: ${kFunction.returnType.simpleName}")
    }
}
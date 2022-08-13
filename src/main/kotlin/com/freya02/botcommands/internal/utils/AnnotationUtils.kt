package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.annotations.api.annotations.AppendMode
import com.freya02.botcommands.annotations.api.annotations.BotPermissions
import com.freya02.botcommands.annotations.api.annotations.UserPermissions
import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.annotations.api.application.annotations.Test
import com.freya02.botcommands.annotations.api.modals.annotations.ModalData
import com.freya02.botcommands.annotations.api.modals.annotations.ModalInput
import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.enumSetOf
import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.javaMethod

object AnnotationUtils {
    //List order is from deepest to most effective
    //aka class --> method
    private fun <A : Annotation> getEffectiveAnnotations(method: KFunction<*>, annotation: KClass<A>): List<A> {
        return method.findAnnotations(annotation) + method.javaMethod!!.declaringClass.getDeclaredAnnotationsByType(annotation.java)
    }

    @JvmStatic
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

    private val optionAnnotations = listOf(TextOption::class, AppOption::class, ModalData::class, ModalInput::class)
    fun isOption(parameter: KParameter) = parameter.annotations.any { it.annotationClass in optionAnnotations }

    @JvmStatic
    fun <A : Annotation> getEffectiveAnnotation(method: KFunction<*>, annotationType: KClass<A>): A? {
        val methodAnnot = method.findAnnotations(annotationType)

        return when {
            methodAnnot.isNotEmpty() -> methodAnnot.first()
            else -> method.javaMethod!!.declaringClass.kotlin.findAnnotations(annotationType).firstOrNull()
        }
    }

    fun getEffectiveTestState(method: KFunction<*>): Boolean {
        return getEffectiveAnnotations(method, Test::class).isNotEmpty()
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
}
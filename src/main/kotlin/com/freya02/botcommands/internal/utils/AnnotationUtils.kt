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
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

internal object AnnotationUtils {
    internal fun getEffectiveTestGuildIds(context: BContextImpl, func: KFunction<*>): TLongSet {
        val set = TLongHashSet(context.applicationConfig.testGuildIds)
        val annotation = func.findAnnotation<Test>() ?: return set

        val methodValue = annotation.guildIds
        set.addAll(methodValue)

        if (!annotation.append && methodValue.isNotEmpty()) {
            return set
        }

        val classValue: LongArray = func.declaringClass.findAnnotation<Test>()?.guildIds ?: LongArray(0)
        set.addAll(classValue)

        return set
    }

    internal fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        val set: EnumSet<Permission> = enumSetOf()
        val annotation = func.findAnnotation<UserPermissions>() ?: return set

        val methodValue = annotation.value
        set += methodValue

        if (annotation.append) {
            val classValue = func.declaringClass.findAnnotation<UserPermissions>()?.value ?: emptyArray()
            set += classValue
        }

        return set
    }

    internal fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        val set: EnumSet<Permission> = enumSetOf()
        val annotation = func.findAnnotation<BotPermissions>() ?: return set

        val methodValue = annotation.value
        set += methodValue

        if (annotation.append) {
            val classValue = func.declaringClass.findAnnotation<BotPermissions>()?.value ?: emptyArray()
            set += classValue
        }

        return set
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T, A : Annotation> getAnnotationValue(annotation: A, methodName: String): T {
        val kFunction = annotation.annotationClass.declaredMemberProperties.find { it.name == methodName }
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}'")
        return kFunction.call(annotation) as? T
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}' as the type is incorrect, annotation type: ${kFunction.returnType.simpleNestedName}")
    }
}
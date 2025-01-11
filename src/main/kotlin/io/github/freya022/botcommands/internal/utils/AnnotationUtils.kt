package io.github.freya022.botcommands.internal.utils

import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import io.github.freya022.botcommands.api.commands.annotations.Filter as FilterAnnotation

internal object AnnotationUtils {
    internal fun getEffectiveTestGuildIds(context: BContext, func: KFunction<*>): TLongSet {
        val set = TLongHashSet(context.applicationConfig.testGuildIds)

        // Always add class values
        func.declaringClass.findAnnotationRecursive<Test>()
            ?.guildIds
            ?.let(set::addAll)

        // Add function values if present
        val functionAnnotation = func.findAnnotationRecursive<Test>()
        if (functionAnnotation != null) {
            if (functionAnnotation.append) {
                set.addAll(functionAnnotation.guildIds)
            } else {
                set.clear()
                set.addAll(functionAnnotation.guildIds)
            }
        }

        return set
    }

    internal fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        val set: EnumSet<Permission> = enumSetOf()

        // Always add class values
        func.declaringClass.findAnnotationRecursive<UserPermissions>()
            ?.permissions
            ?.let(set::addAll)

        // Add function values if present
        val functionAnnotation = func.findAnnotationRecursive<UserPermissions>()
        if (functionAnnotation != null) {
            if (functionAnnotation.append) {
                set += functionAnnotation.permissions
            } else {
                set.clear()
                set += functionAnnotation.permissions
            }
        }

        return set
    }

    internal fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        val set: EnumSet<Permission> = enumSetOf()

        // Always add class values
        func.declaringClass.findAnnotationRecursive<BotPermissions>()
            ?.permissions
            ?.let(set::addAll)

        // Add function values if present
        val functionAnnotation = func.findAnnotationRecursive<BotPermissions>()
        if (functionAnnotation != null) {
            if (functionAnnotation.append) {
                set += functionAnnotation.permissions
            } else {
                set.clear()
                set += functionAnnotation.permissions
            }
        }

        return set
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Filter> getFilters(context: BContext, func: KFunction<*>, filterType: KClass<T>): List<T> {
        return func.findAllAnnotations<FilterAnnotation>()
            .flatMap { it.classes }
            .onEach {
                require(it.isSubclassOf(filterType)) {
                    "Filter ${it.simpleNestedName} must implement ${filterType.simpleNestedName}"
                }
            }
            .map { context.getService(it) as T }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T, A : Annotation> getAnnotationValue(annotation: A, methodName: String): T {
        val kFunction = annotation.annotationClass.declaredMemberProperties.find { it.name == methodName }
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}'")
        return kFunction.call(annotation) as? T
            ?: throwInternal("Could not read '$methodName' from annotation '${annotation.annotationClass.simpleName}' as the type is incorrect, annotation type: ${kFunction.returnType.simpleNestedName}")
    }
}
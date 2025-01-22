package io.github.freya022.botcommands.internal.utils

import gnu.trove.set.TLongSet
import gnu.trove.set.hash.TLongHashSet
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.FilterFactory
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.IFilterFactory
import io.github.freya022.botcommands.api.core.filters.exceptions.InvalidFilterFactoryAnnotationException
import io.github.freya022.botcommands.api.core.filters.exceptions.InvalidFilterTypeException
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.core.utils.flatMap
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.collections.flatMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure
import io.github.freya022.botcommands.api.commands.annotations.Filter as FilterAnnotation

internal object AnnotationUtils {
    internal fun getEffectiveTestGuildIds(context: BContext, func: KFunction<*>): TLongSet {
        val set = TLongHashSet(context.applicationConfig.testGuildIds)
        func.findAllAnnotations<Test>().forEach { set.addAll(it.guildIds) }
        return set
    }

    internal fun getUserPermissions(func: KFunction<*>): EnumSet<Permission> {
        return func.findAllAnnotations<UserPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }

    internal fun getBotPermissions(func: KFunction<*>): EnumSet<Permission> {
        return func.findAllAnnotations<BotPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Filter> getFilters(context: BContext, func: KFunction<*>, filterType: KClass<T>): List<T> {
        val singletonFilters = func.findAllAnnotations<FilterAnnotation>()
            .flatMap { it.classes }
            .onEach {
                requireThrowing(it.isSubclassOf(filterType), ::InvalidFilterTypeException) {
                    "Filter ${it.simpleNestedName} must implement ${filterType.simpleNestedName}"
                }
            }
            .map { context.getService(it) as T }

        val factoryFilters = func.findAllAnnotationsWith<FilterFactory>()
            .flatMap { (customAnnotation, filterFactoryAnnotation) ->
                filterFactoryAnnotation.classes.map { factoryType ->
                    val annotationType = factoryType.superErasureAt<IFilterFactory<*>>(0).jvmErasure
                    requireThrowing(annotationType == customAnnotation.annotationClass, ::InvalidFilterFactoryAnnotationException) {
                        "Filter factory ${factoryType.simpleNestedName} only accepts @${annotationType.shortQualifiedName} but was used by @${customAnnotation.annotationClass.shortQualifiedName}"
                    }

                    val factory = context.getService(factoryType) as IFilterFactory<Annotation>
                    val filter = factory.create(func, customAnnotation)
                    requireThrowing(filterType.isInstance(filter), ::InvalidFilterTypeException) {
                        "Filter ${filter.javaClass.simpleNestedName} returned by ${factoryType.shortQualifiedName} must implement ${filterType.simpleNestedName}"
                    }
                    filter as T
                }
            }

        return singletonFilters + factoryFilters
    }
}
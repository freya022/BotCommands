package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import kotlin.collections.flatMap
import kotlin.reflect.KFunction

/**
 * Base class for any executable method (commands, components, modals...).
 *
 * This never represents an aggregator.
 */
interface Executable {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The target function of this executable.
     *
     * This is strictly for introspection purposes, do not call this function manually.
     */
    val function: KFunction<*>

    /**
     * The parameters of this executable.
     *
     * @see AggregatedParameter
     */
    val parameters: List<AggregatedParameter>

    /**
     * All options from this executable, including from its [aggregates][parameters].
     *
     * These options have no specific order of appearance, use [allOptionsOrdered] instead.
     */
    val allOptions: List<Option>
        get() = parameters.flatMap { it.allOptions }

    /**
     * All options from this executable, including from its [aggregates][parameters],
     * sorted by order of appearance in this function.
     */
    val allOptionsOrdered: List<Option>
        get() = parameters.flatMap { it.allOptionsOrdered }

    /**
     * Returns the aggregated parameter with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     */
    @Deprecated("For removal, confusing on whether it searches nested parameters, " +
            "prefer using collection operations on 'parameters' instead, make an extension or an utility method")
    fun getParameter(declaredName: String): AggregatedParameter?

    /**
     * Returns the option with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, there can be one or more options with the provided name, " +
            "prefer using collection operations on 'allOptions' instead, make an extension or an utility method")
    fun getOptionByDeclaredName(name: String): Option? =
        allOptions.find { it.declaredName == name }

    /**
     * Returns `true` if this element is annotated with [annotationType].
     *
     * The search is breadth-first and considers meta-annotations.
     */
    fun hasAnnotation(annotationType: Class<out Annotation>) = function.hasAnnotationRecursive(annotationType.kotlin)

    /**
     * Finds a single annotation of type [annotationType] from this executable's function.
     *
     * The search is breadth-first and considers meta-annotations.
     */
    fun <A : Annotation> findAnnotation(annotationType: Class<out A>) = function.findAnnotationRecursive(annotationType.kotlin)

    /**
     * Finds all annotations of type [annotationType] from this executable's function.
     *
     * The annotations are not in any specific order, this considers meta-annotations.
     *
     * [@Repeatable][Repeatable] is supported.
     *
     * @param directOverrides Whether a direct annotation should override meta-annotations of the same type
     */
    fun <A : Annotation> findAllAnnotations(annotationType: Class<out A>, directOverrides: Boolean) = function.findAllAnnotations(annotationType.kotlin, directOverrides)

    /**
     * Finds all annotations meta-annotated with [annotationType] from this executable's function.
     *
     * The search is breadth-first and considers meta-annotations.
     */
    fun <A : Annotation> findAllAnnotationsWith(annotationType: Class<out A>) = function.findAllAnnotationsWith(annotationType.kotlin)

    /**
     * Finds all annotations from this executable's function.
     *
     * The search is breadth-first and considers meta-annotations.
     */
    fun getAllAnnotations() = function.getAllAnnotations()
}

/**
 * Returns `true` if this element is annotated with [A].
 *
 * The search is breadth-first and considers meta-annotations.
 */
inline fun <reified A : Annotation> Executable.hasAnnotation() = function.hasAnnotationRecursive<A>()

/**
 * Finds a single annotation of type [A] from this executable's function.
 *
 * The search is breadth-first and considers meta-annotations.
 */
inline fun <reified A : Annotation> Executable.findAnnotation() = function.findAnnotationRecursive<A>()

/**
 * Finds all annotations of type [A] from this executable's function.
 *
 * The annotations are not in any specific order, this considers meta-annotations.
 *
 * [@Repeatable][Repeatable] is supported.
 *
 * @param directOverrides Whether a direct annotation should override meta-annotations of the same type
 */
inline fun <reified A : Annotation> Executable.findAllAnnotations(directOverrides: Boolean = true) = function.findAllAnnotations<A>(directOverrides)

/**
 * Finds all annotations meta-annotated with [A] from this executable's function.
 *
 * The search is breadth-first and considers meta-annotations.
 */
inline fun <reified A : Annotation> Executable.findAllAnnotationsWith() = function.findAllAnnotationsWith<A>()
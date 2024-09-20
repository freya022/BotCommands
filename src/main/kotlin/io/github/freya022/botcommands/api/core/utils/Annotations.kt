@file:JvmName("AnnotationUtils")

package io.github.freya022.botcommands.api.core.utils

import io.github.freya022.botcommands.internal.utils.throwArgument
import java.lang.annotation.Inherited
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty
import kotlin.reflect.safeCast

/**
 * Returns `true` if this [element] is annotated with [annotationType].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
fun hasAnnotationRecursive(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean =
    element.toKAnnotatedElement().hasAnnotationRecursive(annotationType.kotlin)

/**
 * Returns `true` if this element is annotated with [A].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
inline fun <reified A : Annotation> KAnnotatedElement.hasAnnotationRecursive(): Boolean =
    hasAnnotationRecursive(A::class)

/**
 * Returns `true` if this element is annotated with [annotationType].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
fun KAnnotatedElement.hasAnnotationRecursive(annotationType: KClass<out Annotation>): Boolean =
    findAnnotationRecursive(annotationType) != null

/**
 * Finds a single annotation of type [A] from the annotated [element].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
fun <A : Annotation> findAnnotationRecursive(element: AnnotatedElement, annotationType: Class<A>): A? =
    element.toKAnnotatedElement().findAnnotationRecursive(annotationType.kotlin)

/**
 * Finds a single annotation of type [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
inline fun <reified A : Annotation> KAnnotatedElement.findAnnotationRecursive(): A? =
    findAnnotationRecursive(A::class)

/**
 * Finds a single annotation of type [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
fun <A : Annotation> KAnnotatedElement.findAnnotationRecursive(annotationType: KClass<A>): A? {
    var foundAnnotation: A? = null
    bfs(this) {
        val annotation = annotationType.safeCast(it)
        if (annotation != null) {
            foundAnnotation = annotation
            false
        } else {
            true
        }
    }
    return foundAnnotation
}

/**
 * Finds all annotations of type [A] from the annotated [element].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 *
 * [@Repeatable][Repeatable] is supported.
 */
fun <A : Annotation> findAllAnnotations(element: AnnotatedElement, annotationType: Class<A>): List<A> =
    element.toKAnnotatedElement().findAllAnnotations(annotationType.kotlin)

/**
 * Finds all annotations of type [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 *
 * [@Repeatable][Repeatable] is supported.
 */
@JvmSynthetic
inline fun <reified A : Annotation> KAnnotatedElement.findAllAnnotations(): List<A> =
    findAllAnnotations(A::class)

/**
 * Finds all annotations of type [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 *
 * [@Repeatable][Repeatable] is supported.
 */
@JvmSynthetic
fun <A : Annotation> KAnnotatedElement.findAllAnnotations(annotationType: KClass<A>): List<A> = buildList {
    bfs(this@findAllAnnotations) {
        val annotation = annotationType.safeCast(it)
        if (annotation != null)
            this += annotation
        true
    }
}

data class MetaAnnotatedClass<A : Annotation> internal constructor(
    val metaAnnotatedElement: Annotation,
    val metaAnnotation: A
)

/**
 * Finds all annotations meta-annotated with [A] from the annotated [element].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
fun <A : Annotation> findAllAnnotationsWith(element: AnnotatedElement, annotationType: Class<A>): List<MetaAnnotatedClass<A>> =
    element.toKAnnotatedElement().findAllAnnotationsWith(annotationType.kotlin)

/**
 * Finds all annotations meta-annotated with [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
inline fun <reified A : Annotation> KAnnotatedElement.findAllAnnotationsWith(): List<MetaAnnotatedClass<A>> =
    findAllAnnotationsWith(A::class)

/**
 * Finds all annotations meta-annotated with [A] from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@Suppress("UNCHECKED_CAST")
@JvmSynthetic
fun <A : Annotation> KAnnotatedElement.findAllAnnotationsWith(annotationType: KClass<A>): List<MetaAnnotatedClass<A>> = buildList {
    bfs(this@findAllAnnotationsWith) {
        bfs(it.annotationClass) { metaAnnotation ->
            if (metaAnnotation.annotationClass == annotationType) {
                this += MetaAnnotatedClass(it, metaAnnotation as A)
                false
            } else {
                true
            }
        }

        true
    }
}

/**
 * Finds all annotations from the annotated [element].
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
fun getAllAnnotations(element: AnnotatedElement): List<Annotation> =
    element.toKAnnotatedElement().getAllAnnotations()

/**
 * Finds all annotations from the annotated element.
 *
 * The search is breadth-first and considers meta-annotations,
 * but does not support superclasses via [@Inherited][Inherited].
 */
@JvmSynthetic
fun KAnnotatedElement.getAllAnnotations(): List<Annotation> = buildList {
    bfs(this@getAllAnnotations) {
        this += it
        true
    }
}

private fun AnnotatedElement.toKAnnotatedElement(): KAnnotatedElement = when (this) {
    is Class<*> -> kotlin
    is Method -> kotlinFunction ?: throwArgument("Cannot represent as a KFunction: $this")
    is Constructor<*> -> kotlinFunction ?: throwArgument("Cannot represent as a KFunction (constructor): $this")
    is Field -> kotlinProperty ?: throwArgument("Cannot represent as a KProperty: $this")
    else -> throwArgument("Cannot represent as a KAnnotatedElement: $this")
}

private fun bfs(root: KAnnotatedElement, block: (annotation: Annotation) -> Boolean) {
    // The annotation is most likely on the root element, avoid hashSetOf/ArrayDeque allocation
    root.annotations.forEach {
        if (!block(it)) return
    }

    // Block hasn't returned, we need to search deeper
    deepBfs(root, block)
}

private fun deepBfs(root: KAnnotatedElement, block: (annotation: Annotation) -> Boolean) {
    val visited = hashSetOf<KAnnotatedElement>()
    val toVisit = ArrayDeque<KAnnotatedElement>()
    root.annotations.forEach { toVisit.addLast(it.annotationClass) }

    while (toVisit.isNotEmpty()) {
        val element = toVisit.removeFirst()
        if (!visited.add(element)) continue

        element.annotations.forEach {
            toVisit.addLast(it.annotationClass)
            if (!block(it)) return
        }
    }
}
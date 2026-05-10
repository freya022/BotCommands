package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.core.Filter
import kotlin.reflect.KClass

/**
 * References a filtering service, usable for annotated application commands and text commands.
 *
 * ### Merging
 * This annotation can be merged if found with other meta-annotations.
 * Keep in mind that a *direct* annotation overrides all meta-annotations.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Filter(@get:JvmName("value") vararg val classes: KClass<out Filter>)

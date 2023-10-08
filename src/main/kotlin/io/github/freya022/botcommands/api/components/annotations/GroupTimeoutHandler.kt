package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.data.GroupTimeoutData

/**
 * Annotation marking a method as a persistent group timeout handler
 *
 * Requirements:
 * - Public & non-static
 * - First parameter is a [GroupTimeoutData]
 */
@Target(AnnotationTarget.FUNCTION)
annotation class GroupTimeoutHandler(@get:JvmName("value") val name: String)

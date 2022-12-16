package com.freya02.botcommands.api.components.annotations

import com.freya02.botcommands.api.components.data.ComponentTimeoutData

/**
 * Annotation marking a method as a persistent component timeout handler
 *
 * Requirements:
 * - Public & non-static
 * - First parameter is a [ComponentTimeoutData]
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ComponentTimeoutHandler(val name: String)

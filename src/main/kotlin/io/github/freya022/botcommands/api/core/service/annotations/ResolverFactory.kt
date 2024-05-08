package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Marks this class as a [parameter resolver factory][ParameterResolverFactory].<br>
 * This is a specialization of [@BService][BService] for parameter resolver factories.
 *
 * **Requirement:** this must extend [ParameterResolverFactory].
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * @see BService @BService
 *
 * @see ParameterResolverFactory
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Bean
@Component
annotation class ResolverFactory
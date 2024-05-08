package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Marks this class as a [parameter resolver][ParameterResolver].<br>
 * This is a specialization of [@BService][BService] for parameter resolvers.
 *
 * **Requirement:** this must extend [ParameterResolver].
 *
 * **Warning:** Top-level functions are not processed, you must have them in an object/class.
 *
 * @see BService @BService
 *
 * @see ClassParameterResolver
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Bean
@Component
annotation class Resolver
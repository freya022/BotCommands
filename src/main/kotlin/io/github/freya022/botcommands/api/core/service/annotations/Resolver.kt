package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
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
@BService
annotation class Resolver(
    /**
     * The priority of this resolver (then wrapped as a resolver factory).
     *
     * When getting a resolver factory, the factory with the highest value that is [resolvable][ParameterResolverFactory.isResolvable] is taken.
     *
     * If two factories with the same priority exist and are both resolvable, an exception is thrown.
     *
     * @see ParameterResolverFactory.priority
     */
    val priority: Int = 0
)
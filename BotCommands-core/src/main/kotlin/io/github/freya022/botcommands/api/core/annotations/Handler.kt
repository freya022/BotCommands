package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Enables this class to be scanned for one or more handler.<br>
 * This is a specialization of [@BService][BService] for handlers,
 * such as for components, modals and autocomplete.
 *
 * A warning will be logged if this class does not have any handlers,
 * i.e., methods that declare handlers with annotations.
 *
 * @see BService @BService
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Bean
@Component
@BService
annotation class Handler

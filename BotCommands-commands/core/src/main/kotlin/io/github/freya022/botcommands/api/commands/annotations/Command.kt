package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Enables this class to be scanned for one or more commands.<br>
 * This is a specialization of [@BService][BService] for commands.
 *
 * A warning will be logged if this class does not have any commands,
 * i.e., methods that declare commands with annotations, or methods that declare using the DSL.
 *
 * @see BService @BService
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Bean
@Component
@BService
annotation class Command

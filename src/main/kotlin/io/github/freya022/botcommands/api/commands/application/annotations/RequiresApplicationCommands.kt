package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.commands.application.RequiresApplicationCommandsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if application commands are [not enabled][BApplicationConfig.enable].
 *
 * @see BApplicationConfig.enable
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresApplicationCommandsChecker::class)
@ConditionalOnProperty("botcommands.application.enable")
annotation class RequiresApplicationCommands
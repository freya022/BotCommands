package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.commands.text.RequiresTextCommandsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if text commands are [not enabled][BTextConfig.enable].
 *
 * @see BTextConfig.enable
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresTextCommandsChecker::class)
@ConditionalOnProperty("botcommands.text.enable")
annotation class RequiresTextCommands
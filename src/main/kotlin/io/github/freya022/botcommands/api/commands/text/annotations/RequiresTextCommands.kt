package io.github.freya022.botcommands.api.commands.text.annotations

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.commands.text.RequiresTextCommandsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresTextCommandsChecker::class)
@ConditionalOnProperty("botcommands.text.enable")
annotation class RequiresTextCommands
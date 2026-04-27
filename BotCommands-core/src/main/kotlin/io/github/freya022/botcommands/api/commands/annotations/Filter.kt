package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.Filter
import kotlin.reflect.KClass

/**
 * References a filtering service, usable for annotated application commands and text commands.
 *
 * ### Requirements
 * The filter must implement at least one of the corresponding interfaces:
 * - [@JDASlashCommand][JDASlashCommand], [@JDAUserCommand][JDAUserCommand], [@JDAMessageCommand][JDAMessageCommand] -> [ApplicationCommandFilter]
 *
 * ### Merging
 * This annotation can be merged if found with other meta-annotations.
 * Keep in mind that a *direct* annotation overrides all meta-annotations.
 *
 * @see ApplicationCommandFilter
 *
 * @see ApplicationCommandBuilder.filter DSL equivalent (application commands)
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Filter(@get:JvmName("value") vararg val classes: KClass<out Filter>)

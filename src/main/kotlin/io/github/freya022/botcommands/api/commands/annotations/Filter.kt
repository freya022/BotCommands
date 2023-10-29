package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.commands.text.builder.filter
import io.github.freya022.botcommands.api.components.builder.IActionableComponent
import io.github.freya022.botcommands.api.components.builder.filter
import io.github.freya022.botcommands.api.core.Filter
import kotlin.reflect.KClass

/**
 * References an interaction filtering service.
 *
 * The filter must implement at least one of the corresponding interfaces:
 * - [@JDASlashCommand][JDASlashCommand], [@JDAUserCommand][JDAUserCommand], [@JDAMessageCommand][JDAMessageCommand] -> [ApplicationCommandFilter]
 * - [@JDATextCommand][JDATextCommand] -> [TextCommandFilter]
 *
 * **Note:** This **cannot** be used on component handlers,
 * use the appropriate methods on the builders instead like [IActionableComponent.filter] / [IActionableComponent.addFilter].
 *
 * @see ApplicationCommandFilter
 * @see TextCommandFilter
 *
 * @see ApplicationCommandBuilder.filter DSL equivalent (application commands)
 * @see TextCommandVariationBuilder.filter DSL equivalent (text commands)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Filter(@get:JvmName("value") vararg val classes: KClass<out Filter>)

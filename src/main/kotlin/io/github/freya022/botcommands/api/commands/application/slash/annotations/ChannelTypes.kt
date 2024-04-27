package io.github.freya022.botcommands.api.commands.application.slash.annotations

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * Sets the desired channel types for this parameter.
 *
 * This works for annotated commands as well as code-declared commands
 *
 * You can alternatively use a specific channel type,
 * such as [TextChannel] to automatically restrict the channel type.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelTypes(vararg val value: ChannelType)

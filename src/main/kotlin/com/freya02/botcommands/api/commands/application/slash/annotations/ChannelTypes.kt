package com.freya02.botcommands.api.commands.application.slash.annotations

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * Sets the desired channel types for this [SlashOption].
 *
 * You can alternatively use a specific channel type,
 * such as [TextChannel] to automatically restrict the channel type.
 *
 * @see SlashCommandOptionBuilder.channelTypes DSL equivalent
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelTypes(vararg val value: ChannelType)

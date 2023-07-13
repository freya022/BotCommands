package com.freya02.botcommands.api.commands.application.slash.annotations;


import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;

/**
 * Sets the desired channel types for this {@link AppOption}.
 *
 * <p>You can alternatively use a specific channel type,
 * such as {@link TextChannel} to automatically restrict the channel type.
 *
 * @see SlashCommandOptionBuilder#setChannelTypes(EnumSet) DSL equivalent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ChannelTypes {
	ChannelType[] value();
}

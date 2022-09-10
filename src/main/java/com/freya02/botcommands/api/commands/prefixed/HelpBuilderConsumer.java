package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * See {@link #accept(EmbedBuilder, boolean, TextCommandInfo)} for details
 */
public interface HelpBuilderConsumer {
	/**
	 * The function called when building an help embed
	 *
	 * @param builder     The {@link EmbedBuilder} to fill / override
	 * @param isGlobal    <code>true</code> if the embed is showing all the commands, <code>false</code> if the embed is for a specific command
	 * @param commandInfo The text command to retrieve the help from
	 *                    <br>Will be null if <code>isGlobal</code> is <code>true</code>
	 */
	void accept(@NotNull EmbedBuilder builder, boolean isGlobal, @Nullable TextCommandInfo commandInfo);
}

package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.internal.prefixed.TextCommandCandidates;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * See {@link #accept(EmbedBuilder, boolean, TextCommandCandidates)} for details
 */
public interface HelpBuilderConsumer {
	/**
	 * The function called when building an help embed
	 *
	 * @param builder    The {@link EmbedBuilder} to fill / override
	 * @param isGlobal   <code>true</code> if the embed is showing all the commands, <code>false</code> if the embed is for a specific command
	 * @param candidates A list of text commands, those commands share the same prefix, but may have input variations (i.e. a command with the same path, but with different arguments)
	 *                   <br>Will be null if <code>isGlobal</code> is <code>true</code>
	 */
	void accept(@NotNull EmbedBuilder builder, boolean isGlobal, @Nullable TextCommandCandidates candidates);
}

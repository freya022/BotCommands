package io.github.freya022.botcommands.api.commands.text;

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A consumer that's called when a help embed is about to be sent.
 * <br>That embed can be for the command list as well as individual commands.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see #accept(EmbedBuilder, boolean, TextCommandInfo)
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
public interface HelpBuilderConsumer {
	/**
	 * The function called when building a help embed
	 *
	 * @param builder     The {@link EmbedBuilder} to fill / override
	 * @param isGlobal    {@code true} if the embed is showing all the commands,
	 *                    {@code false} if the embed is for a specific command
	 * @param commandInfo The text command to retrieve the help from
	 *                    <br>Will be null if {@code isGlobal} is {@code true}
	 */
	void accept(@NotNull EmbedBuilder builder, boolean isGlobal, @Nullable TextCommandInfo commandInfo);
}

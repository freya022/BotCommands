package com.freya02.botcommands.api.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.CommandId;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.annotations.Default;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DefaultValueResolver {
	@Nullable
	default Boolean isDefaultValueEnabled(@NotNull BContext context, @NotNull Guild guild,
	                                      @Nullable String commandId, @NotNull CommandPath commandPath,
	                                      @NotNull String optionName, @NotNull Class<?> parameterType) {
		return null;
	}

	/**
	 * Returns the default value of an {@link AppOption}, for slash commands only
	 * <br>This method is called only if your option is annotated with {@link Default}
	 * <p>This method will only be called once per command option per guild
	 *
	 * @param context       The current BotCommands context
	 * @param guild         The {@link Guild} in which to add the default value
	 * @param commandId     The ID of the command, as optionally set in {@link CommandId}, might be <code>null</code>
	 * @param commandPath   The path of the command, as set in {@link JDASlashCommand}
	 * @param optionName    The name of the <b>transformed</b> command option, might not be equal to the parameter name
	 * @param parameterType The <b>boxed</b> type of the command option
	 *
	 * @return A DefaultValue object if the option can be default-ed with a nullable object
	 */
	@Nullable
	default DefaultValue getDefaultValue(@NotNull BContext context, @NotNull Guild guild,
	                                     @Nullable String commandId, @NotNull CommandPath commandPath,
	                                     @NotNull String optionName, @NotNull Class<?> parameterType) {
		return null;
	}
}

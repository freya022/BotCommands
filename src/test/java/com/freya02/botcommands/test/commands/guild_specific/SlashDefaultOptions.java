package com.freya02.botcommands.test.commands.guild_specific;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashDefaultOptions extends ApplicationCommand {
	private static final String STR_AUTOCOMPLETE_NAME = "SlashDefaultOptions: str";

	@Override
	public @NotNull GeneratedValueSupplier getGeneratedValueSupplier(@Nullable Guild guild,
	                                                                 @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                                 @NotNull String optionName, @NotNull ParameterType type) {
		if (guild.getIdLong() != 722891685755093072L) { //Push default values only outside the test guild
			if (commandPath.toString().equals("default")) {
				if (optionName.equals("defaulted_string")) {
					return event -> "default str";
				}
			}
		}

		return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, type);
	}

	@JDASlashCommand(name = "default")
	public void run(GuildSlashEvent event, @AppOption String defaultedString, @AppOption(autocomplete = STR_AUTOCOMPLETE_NAME) String str) {
		event.reply("String: " + defaultedString)
				.setEphemeral(true)
				.queue();
	}

	@AutocompleteHandler(name = STR_AUTOCOMPLETE_NAME)
	public Collection<String> onStrAutocomplete(CommandAutoCompleteInteractionEvent event, @AppOption String defaultedString) {
		return List.of(defaultedString);
	}
}

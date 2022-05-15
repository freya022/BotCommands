package com.freya02.botcommands.test.commands.guild_specific;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashDefaultOptions extends ApplicationCommand {
	private static final String STR_AUTOCOMPLETE_NAME = "SlashDefaultOptions: str";

	@Override
	@Nullable
	public DefaultValueSupplier getDefaultValueSupplier(@NotNull BContext context, @NotNull Guild guild,
	                                                    @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                    @NotNull String optionName, @NotNull KType type, @NotNull KClass<?> parameterType) {
		if (guild.getIdLong() != 722891685755093072L) { //Push default values only outside the test guild
			if (commandPath.toString().equals("default")) {
				if (optionName.equals("defaulted_string")) {
					return event -> "default str";
				}
			}
		}

		return super.getDefaultValueSupplier(context, guild, commandId, commandPath, optionName, type, parameterType);
	}

	@JDASlashCommand(name = "default")
	public void run(GuildSlashEvent event, @AppOption String defaultedString, @AppOption(autocomplete = STR_AUTOCOMPLETE_NAME) String str) {
		event.reply("String: " + defaultedString)
				.setEphemeral(true)
				.queue();
	}

	@AutocompletionHandler(name = STR_AUTOCOMPLETE_NAME)
	public Collection<String> onStrAutocomplete(CommandAutoCompleteInteractionEvent event, @AppOption String defaultedString) {
		return List.of(defaultedString);
	}
}

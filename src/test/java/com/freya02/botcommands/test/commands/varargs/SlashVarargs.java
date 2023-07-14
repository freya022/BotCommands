package com.freya02.botcommands.test.commands.varargs;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs;
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashVarargs extends ApplicationCommand {
	private static final String STR_AUTOCOMPLETE_NAME = "SlashVarargs: str";

	@Override
	public @NotNull ApplicationGeneratedValueSupplier getGeneratedValueSupplier(@Nullable Guild guild,
	                                                                            @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                                            @NotNull String optionName, @NotNull ParameterType type) {
//		if (optionName.equals("number")) {
//			return e -> List.of(42L, 43L, 44L);
//		}

		return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, type);
	}

	@JDASlashCommand(name = "varargs")
	public void run(GuildSlashEvent event,
	                @SlashOption(autocomplete = STR_AUTOCOMPLETE_NAME) String string,
	                @SlashOption(name = "number", description = "lol") @VarArgs(value = 3, numRequired = 2) List<Long> longs) {
		event.reply("longs: " + longs)
				.setEphemeral(true)
				.queue();
	}

	@AutocompleteHandler(name = STR_AUTOCOMPLETE_NAME)
	public Collection<String> onStrAutocomplete(CommandAutoCompleteInteractionEvent event, @SlashOption(name = "number") @VarArgs(3) List<Long> longs, @SlashOption String string) {
		return List.of(
				string + "_" + "str1" + "_" + longs.get(0),
				string + "_" + "str2" + "_" + longs.get(1),
				string + "_" + "str3" + "_" + longs.get(2),
				string + "_" + "str4"
		);
	}
}

package com.freya02.botcommands.test.commands.varargs;

import com.freya02.botcommands.annotations.api.application.annotations.AppOption;
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.annotations.api.application.slash.annotations.VarArgs;
import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashVarargs extends ApplicationCommand {
	private static final String STR_AUTOCOMPLETE_NAME = "SlashVarargs: str";

	@Override
	@Nullable
	public DefaultValueSupplier getDefaultValueSupplier(@NotNull BContext context, @NotNull Guild guild,
	                                                    @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                    @NotNull String optionName, @NotNull Class<?> parameterType) {
//		if (optionName.equals("number")) {
//			return e -> List.of(42L, 43L, 44L);
//		}

		return super.getDefaultValueSupplier(context, guild, commandId, commandPath, optionName, parameterType);
	}

	@JDASlashCommand(name = "varargs")
	public void run(GuildSlashEvent event,
	                @AppOption(autocomplete = STR_AUTOCOMPLETE_NAME) String string,
	                @AppOption(name = "number", description = "lol") @VarArgs(value = 3, numRequired = 2) List<Long> longs) {
		event.reply("longs: " + longs)
				.setEphemeral(true)
				.queue();
	}

	@AutocompletionHandler(name = STR_AUTOCOMPLETE_NAME)
	public Collection<String> onStrAutocomplete(CommandAutoCompleteInteractionEvent event, @AppOption(name = "number") @VarArgs(3) List<Long> longs, @AppOption String string) {
		return List.of(
				string + "_" + "str1" + "_" + longs.get(0),
				string + "_" + "str2" + "_" + longs.get(1),
				string + "_" + "str3" + "_" + longs.get(2),
				string + "_" + "str4"
		);
	}
}

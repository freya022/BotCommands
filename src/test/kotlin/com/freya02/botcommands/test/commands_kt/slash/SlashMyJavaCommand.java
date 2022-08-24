package com.freya02.botcommands.test.commands_kt.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.annotations.GeneratedOption;
import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.parameters.ParameterType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class SlashMyJavaCommand extends ApplicationCommand {
	@Override
	@NotNull
	public ApplicationGeneratedValueSupplier getGeneratedValueSupplier(@Nullable Guild guild,
	                                                                   @Nullable String commandId,
	                                                                   @NotNull CommandPath commandPath,
	                                                                   @NotNull String optionName,
	                                                                   @NotNull ParameterType parameterType) {
		if (optionName.equals("guild_name")) {
			return event -> event.getGuild().getName();
		}

		return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType);
	}

	@Override
	@NotNull
	public List<Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String optionName) {
		if (optionName.equals("string_option") || optionName.equals("string_annotated")) {
			return List.of(new Choice("a", "a"), new Choice("b", "b"), new Choice("c", "c"));
		} else if (optionName.equals("int_option") || optionName.equals("int_annotated")) {
			return List.of(new Choice("1", 1L), new Choice("2", 2L));
		}

		return super.getOptionChoices(guild, commandPath, optionName);
	}

	@JDASlashCommand(name = "my_command_annotated", subcommand = "java", description = "mah desc")
	public void cmd(GuildSlashEvent event,
	                @AppOption(name = "string_annotated", description = "Option description") String stringOption,
	                @AppOption(name = "int_annotated", description = "An integer") @LongRange(from = 1, to = 2) int intOption,
	                @AppOption(name = "user_annotated", description = "An user") User userOption,
	                @AppOption(name = "channel_annotated") GuildChannel channelOption,
	                @AppOption(name = "autocomplete_str_annotated", description = "Autocomplete !", autocomplete = SlashMyCommand.autocompleteHandlerName) String autocompleteStr,
	                @AppOption(name = "double_annotated", description = "A double") @Optional double doubleOption,
	                BContext custom,
	                @GeneratedOption String guildName) {
		event.reply(stringOption + intOption + doubleOption + userOption + custom + channelOption + autocompleteStr + guildName).queue();
	}
}

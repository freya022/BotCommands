package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SlashUtils {
	public static void appendCommands(List<Command> commands, StringBuilder sb) {
		for (Command command : commands) {
			final StringJoiner joiner = new StringJoiner("] [", "[", "]").setEmptyValue("");
			for (Command.Option option : command.getOptions()) {
				joiner.add(option.getType().name());
			}

			sb.append(" - ").append(command.getName()).append(" ").append(joiner).append("\n");
		}
	}

	@NotNull
	public static List<List<Command.Choice>> getOptionChoices(@Nullable Guild guild, ApplicationCommandInfo info) {
		List<List<Command.Choice>> optionsChoices = new ArrayList<>();

		final int count = info.getParameters().getOptionCount();
		for (int optionIndex = 0; optionIndex < count; optionIndex++) {
			optionsChoices.add(getChoicesForCommandOption(guild, info, optionIndex));
		}

		return optionsChoices;
	}

	@NotNull
	private static List<Command.Choice> getChoicesForCommandOption(@Nullable Guild guild, ApplicationCommandInfo info, int optionIndex) {
		return ((ApplicationCommand) info.getInstance()).getOptionChoices(guild, info.getPath(), optionIndex); //TODO change to use opaque user data
	}
}

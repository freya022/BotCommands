package com.freya02.botcommands;

import com.freya02.botcommands.annotation.JdaCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@JdaCommand(
		name = "help",
		description = "Gives help about a command",
		category = "Utils"
)
final class HelpCommand extends Command {
	private static class CommandDescription {
		private final String name, description;

		private CommandDescription(String name, String description) {
			this.name = name;
			this.description = description;
		}
	}

	private final EmbedBuilder generalHelpBuilder;

	private final Map<String, EmbedBuilder> cmdToEmbed = new HashMap<>();
	private final EmbedBuilder defaultEmbed;

	//Method can't have direct CommandInfo object as the HelpCommand is built before it is put in the command map
	private void addDetailedHelp(Command command, String name, String description, boolean addSubcommandHelp, List<CommandInfo> subcommandsInfo) {
		final EmbedBuilder builder = new EmbedBuilder(defaultEmbed);
		final MessageEmbed.AuthorInfo author = defaultEmbed.build().getAuthor();
		if (author != null) {
			builder.setAuthor(author.getName() + " – '" + name + "' command", author.getUrl(), author.getIconUrl());
		} else {
			builder.setAuthor('\'' + name + "' command");
		}
		builder.addField("Description", description, false);

		if (addSubcommandHelp) {
			final String subcommandHelp = subcommandsInfo.stream().filter(info2 -> !info2.isHidden()).map(info2 -> "**" + info2.getName() + "** : " + info2.getDescription()).collect(Collectors.joining("\r\n"));
			builder.addField("Subcommands", subcommandHelp, false);
		}

		final Consumer<EmbedBuilder> descConsumer = command.getDetailedDescription();
		if (descConsumer != null) {
			descConsumer.accept(builder);
		}

		cmdToEmbed.put(name, builder);
	}

	public HelpCommand(Supplier<EmbedBuilder> defaultEmbedSupplier, Map<String, CommandInfo> commands) {
		this.defaultEmbed = defaultEmbedSupplier.get();
		this.generalHelpBuilder = new EmbedBuilder(this.defaultEmbed);

		Map<String, List<CommandDescription>> categoryToDesc = new HashMap<>();
		for (CommandInfo info : Set.copyOf(commands.values())) {
			//Map category to list of commands
			if (!info.isHidden()) {
				categoryToDesc
						.computeIfAbsent(info.getCategory(), s -> new ArrayList<>())
						.add(
								new CommandDescription(info.getName(), info.getDescription())
						);
			}

			addDetailedHelp(info.getCommand(), info.getName(), info.getDescription(), info.isAddSubcommandHelp(), info.getSubcommandsInfo());
		}

		boolean addedHelpEntry = false;
		for (Map.Entry<String, List<CommandDescription>> entry : categoryToDesc.entrySet()) {
			StringBuilder categoryBuilder = new StringBuilder();
			for (CommandDescription description : entry.getValue()) {
				categoryBuilder.append("**").append(description.name).append("** : ").append(description.description).append("\r\n");
			}

			if (entry.getKey().equalsIgnoreCase("utils")) {
				//Add the help entry of this one
				generalHelpBuilder.addField(entry.getKey(), categoryBuilder.toString().trim() + "\r\n**help** : Gives help about a command", false);
				addedHelpEntry = true;
			} else {
				generalHelpBuilder.addField(entry.getKey(), categoryBuilder.toString().trim(), false);
			}
		}

		if (!addedHelpEntry) {
			generalHelpBuilder.addField("Utils", "**help** : Gives help about a command", false);
		}

		//Add the precise help of this one
		addDetailedHelp(this, "help", "Gives help about a command", false, List.of());
	}

	@Override
	public void execute(CommandEvent event) {
		if (event.hasNext(String.class)) {
			getCommandHelp(event, event.nextArgument(String.class));
		} else {
			getAllHelp(event);
		}
	}

	private synchronized void getAllHelp(CommandEvent event) {
		generalHelpBuilder.setTimestamp(Instant.now());
		final Member member = event.getMember();
		if (member != null) {
			generalHelpBuilder.setColor(member.getColorRaw());
		}

		final MessageEmbed embed = generalHelpBuilder.build();
		event.getAuthor().openPrivateChannel().queue(
				privateChannel -> event.sendWithEmbedFooterIcon(privateChannel, event.getDefaultIconStream(), embed, x -> event.getMessage().addReaction("✅").queue(), event.failureReporter("Unable to send help message")),
				t -> event.getChannel().sendMessage("Your DMs are not open").queue());

	}

	private synchronized void getCommandHelp(CommandEvent event, String cmdName) {
		final CommandInfo info = event.getCommandInfo(cmdName);

		if (info == null || info.isHidden()) {
			event.getChannel().sendMessage("Command '" + cmdName + "' does not exists").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		final EmbedBuilder builder = cmdToEmbed.get(cmdName);
		builder.setTimestamp(Instant.now());

		final Member member = event.getMember();
		if (member != null) {
			builder.setColor(member.getColorRaw());
		}

		event.sendWithEmbedFooterIcon(event.getChannel(), event.getDefaultIconStream(), builder.build(), null, event.failureReporter("Unable to send help message"));
	}

	@Override
	public Consumer<EmbedBuilder> getDetailedDescription() {
		return builder -> {
			builder.addField("Usage :", "help [command_name]", false);
			builder.addField("Example :", "help crabrave", false);
		};
	}
}

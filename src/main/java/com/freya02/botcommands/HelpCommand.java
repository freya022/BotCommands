package com.freya02.botcommands;

import com.freya02.botcommands.annotation.JdaCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@JdaCommand(
		name = "help",
		description = "Gives help about a command",
		category = "Utils"
)
final class HelpCommand extends Command {
	private final BContext context;

	private static class CommandDescription {
		private final String name, description;

		private CommandDescription(String name, String description) {
			this.name = name;
			this.description = description;
		}
	}

	private final EmbedBuilder generalHelpBuilder;
	private final EmbedBuilder ownerHelpBuilder;

	public HelpCommand(BContext context) {
		super(context);

		this.context = context;
		this.generalHelpBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());
		this.ownerHelpBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());
	}

	void generate() {
		fillHelp(generalHelpBuilder, info -> !info.isHidden());
		fillHelp(ownerHelpBuilder, info -> true);
	}

	private void fillHelp(EmbedBuilder builder, Function<CommandInfo, Boolean> shouldAddFunc) {
		Map<String, List<CommandDescription>> categoryToDesc = new HashMap<>();
		for (Command command : ((BContextImpl) context).getCommands()) {
			final CommandInfo info = command.getInfo();

			//Map category to list of commands
			if (shouldAddFunc.apply(info)) {
				categoryToDesc
						.computeIfAbsent(info.getCategory(), s -> new ArrayList<>())
						.add(
								new CommandDescription(info.getName(), info.getDescription())
						);
			}
		}

		for (Map.Entry<String, List<CommandDescription>> entry : categoryToDesc.entrySet()) {
			StringBuilder categoryBuilder = new StringBuilder();
			for (CommandDescription description : entry.getValue()) {
				categoryBuilder.append("**").append(description.name).append("** : ").append(description.description).append("\r\n");
			}

			builder.addField(entry.getKey(), categoryBuilder.toString().trim(), false);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.hasNext(String.class)) {
			getCommandHelp(event, event.nextArgument(String.class));
		} else {
			getAllHelp(event);
		}
	}

	private synchronized void getAllHelp(BaseCommandEvent event) {
		final EmbedBuilder builder = event.getContext().isOwner(event.getAuthor().getIdLong()) ? ownerHelpBuilder : getMemberHelpContent(event.getMember());

		builder.setTimestamp(Instant.now());
		final Member member = event.getMember();
		builder.setColor(member.getColorRaw());

		final MessageEmbed embed = builder.build();
		event.getAuthor().openPrivateChannel().queue(
				privateChannel -> event.sendWithEmbedFooterIcon(privateChannel, embed, event.failureReporter("Unable to send help message")).queue(
						m -> event.reactSuccess().queue(),
						t -> event.reactError().queue()),
				t -> event.getChannel().sendMessage("Your DMs are not open").queue());

	}

	private EmbedBuilder getMemberHelpContent(Member member) {
		final EmbedBuilder builder = context.getDefaultEmbedSupplier().get();

		fillHelp(builder, info -> !info.isHidden() && !info.isRequireOwner() && member.hasPermission(info.getUserPermissions()));

		return builder;
	}

	private void getCommandHelp(CommandEvent event, String cmdName) {
		Command cmd = event.getContext().findCommand(cmdName);
		if (cmd == null) {
			event.respond("Command '" + cmdName + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		if (event.hasNext(String.class)) {
			final String subname = event.nextArgument(String.class);

			for (Command subcommand : cmd.getInfo().getSubcommands()) {
				if (subcommand.getInfo().getName().equals(subname)) {
					cmd = subcommand;
					break;
				}
			}
		}

		Command recurCmd = cmd;
		do {
			if (recurCmd.getInfo().isHidden() && !event.getContext().isOwner(event.getAuthor().getIdLong())) {
				event.respond("Command '" + recurCmd.getInfo().getName() + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
				return;
			}
		} while ((recurCmd = recurCmd.getInfo().getParentCommand()) != null);

		cmd.showHelp(event);
	}

	@Override
	public Consumer<EmbedBuilder> getDetailedDescription() {
		return builder -> {
			builder.addField("Usage :", "help [command_name]", false);
			builder.addField("Example :", "help crabrave", false);
		};
	}
}

package com.freya02.botcommands.prefixed;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Usability;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@JdaCommand(
		name = "help",
		description = "Gives help about a command",
		category = "Utils"
)
public final class HelpCommand extends Command {
	private final BContext context;

	private final EmbedBuilder ownerHelpBuilder;

	public HelpCommand(BContext context) {
		super(context);

		this.context = context;
		this.ownerHelpBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());
	}

	public void generate() {
		fillHelp(ownerHelpBuilder, info -> true);
	}

	private void fillHelp(EmbedBuilder builder, Function<CommandInfo, Boolean> shouldAddFunc) {
		TreeMap<String, StringJoiner> categoryBuilderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		Set<String> insertedCommands = new HashSet<>();
		for (Command cmd : ((BContextImpl) context).getCommands()) {
			final CommandInfo info = cmd.getInfo();

			if (insertedCommands.add(info.getName())) {
				if (shouldAddFunc.apply(info)) {
					categoryBuilderMap
							.computeIfAbsent(info.getCategory(), s -> new StringJoiner("\n"))
							.add("**" + info.getName() + "** : " + info.getDescription());
				}
			}
		}

		for (Map.Entry<String, StringJoiner> entry : categoryBuilderMap.entrySet()) {
			builder.addField(entry.getKey(), entry.getValue().toString(), false);
		}

		final Consumer<EmbedBuilder> helpBuilderConsumer = context.getHelpBuilderConsumer();
		if (helpBuilderConsumer != null) helpBuilderConsumer.accept(builder);
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
		final EmbedBuilder builder = event.getContext().isOwner(event.getAuthor().getIdLong()) ? ownerHelpBuilder : getMemberHelpContent(event.getMember(), event.getChannel());

		builder.setTimestamp(Instant.now());
		final Member member = event.getMember();
		builder.setColor(member.getColorRaw());

		final MessageEmbed embed = builder.build();
		event.getAuthor().openPrivateChannel().queue(
				privateChannel -> event.sendWithEmbedFooterIcon(privateChannel, embed, event.failureReporter("Unable to send help message")).queue(
						m -> event.reactSuccess().queue(),
						t -> event.reactError().queue()),
				t -> event.getChannel().sendMessage(context.getDefaultMessages(event.getGuild()).getClosedDMErrorMsg()).queue());

	}

	private EmbedBuilder getMemberHelpContent(Member member, TextChannel channel) {
		final EmbedBuilder builder = context.getDefaultEmbedSupplier().get();

		fillHelp(builder, info -> Usability.of(info, member, channel, !context.isOwner(member.getIdLong())).isShowable());

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

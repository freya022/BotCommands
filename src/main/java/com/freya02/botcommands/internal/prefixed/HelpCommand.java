package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.prefixed.*;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Usability;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Category("Utils")
@Description("Gives help about a command")
public final class HelpCommand extends TextCommand implements IHelpCommand {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	private final BContext context;

	public HelpCommand(BContext context) {
		this.context = context;
	}

	@JDATextCommand(
			name = "help",
			description = "Gives help for all commands"
	)
	public void execute(CommandEvent event) {
		sendGlobalHelp(event);
	}

	@JDATextCommand(
			name = "help",
			description = "Gives help about a command"
	)
	public void execute(BaseCommandEvent event, @TextOption(name = "command path", example = "help") String commandStr) {
		final String[] split = SPACE_PATTERN.split(commandStr);

		if (split.length > 3) {
			event.respond("The command '" + commandStr + "' cannot have more than 3 components").queue();

			return;
		}

		sendCommandHelp(event, CommandPath.of(split));
	}

	@Override
	public void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull CommandPath executedCommandPath) {
		sendCommandHelp(event, executedCommandPath);
	}

	private void sendGlobalHelp(BaseCommandEvent event) {
		final EmbedBuilder builder = generateGlobalHelp(event.getMember(), event.getGuildChannel());

		final MessageEmbed embed = builder.build();
		event.getAuthor().openPrivateChannel().queue(
				privateChannel -> event.sendWithEmbedFooterIcon(privateChannel, embed, event.failureReporter("Unable to send help message")).queue(
						m -> event.reactSuccess().queue(),
						t -> event.reactError().queue()),
				t -> event.getChannel().sendMessage(context.getDefaultMessages(event.getGuild()).getClosedDMErrorMsg()).queue());

	}

	private void sendCommandHelp(BaseCommandEvent event, CommandPath cmdPath) {
		TextCommandCandidates candidates = event.getContext().findCommands(cmdPath);
		if (candidates == null) {
			event.respond("Command '" + getSpacedPath(cmdPath) + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		final Member member = event.getMember();
		final GuildMessageChannel channel = event.getGuildChannel();
		final Usability usability = Usability.of(context, candidates.first(), member, channel, !context.isOwner(member.getIdLong()));
		if (usability.isNotShowable()) {
			event.respond("Command '" + getSpacedPath(cmdPath) + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		final EmbedBuilder embed = generateCommandHelp(event, candidates);

		event.respond(embed.build()).queue();
	}

	private EmbedBuilder generateGlobalHelp(Member member, GuildMessageChannel channel) {
		final EmbedBuilder builder = context.getDefaultEmbedSupplier().get();

		builder.setTimestamp(Instant.now());
		builder.setColor(member.getColorRaw());
		builder.setFooter("NSFW commands might not be shown\nRun help in an NSFW channel to see them\n");

		final TreeMap<String, StringJoiner> categoryBuilderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		final Set<String> insertedCommands = new HashSet<>();
		for (TextCommandCandidates candidates : ((BContextImpl) context).getCommands()) {
			final TextCommandInfo cmd = candidates.first();

			if (insertedCommands.add(cmd.getPath().getName())) { //Prevent duplicates since candidates share the same command path
				if (Usability.of(context, cmd, member, channel, !context.isOwner(member.getIdLong())).isShowable()) {
					categoryBuilderMap
							.computeIfAbsent(Utils.getCategory(cmd), s -> new StringJoiner("\n"))
							.add("**" + cmd.getPath().getName() + "** : " + Utils.getNonBlankDescription(cmd));
				}
			}
		}

		for (Map.Entry<String, StringJoiner> entry : categoryBuilderMap.entrySet()) {
			builder.addField(entry.getKey(), entry.getValue().toString(), false);
		}

		final HelpBuilderConsumer helpBuilderConsumer = context.getHelpBuilderConsumer();
		if (helpBuilderConsumer != null) helpBuilderConsumer.accept(builder, true, null);

		return builder;
	}

	@NotNull
	private EmbedBuilder generateCommandHelp(BaseCommandEvent event, TextCommandCandidates candidates) {
		final EmbedBuilder builder = Utils.generateCommandHelp(candidates, event);
		builder.setTimestamp(Instant.now());
		builder.setColor(event.getMember().getColorRaw());

		final HelpBuilderConsumer helpBuilderConsumer = context.getHelpBuilderConsumer();
		if (helpBuilderConsumer != null) helpBuilderConsumer.accept(builder, false, candidates);

		return builder;
	}

	@NotNull
	private String getSpacedPath(CommandPath cmdPath) {
		return cmdPath.toString().replace('/', ' ');
	}
}

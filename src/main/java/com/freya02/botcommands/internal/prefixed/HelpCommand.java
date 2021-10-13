package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Usability;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@Category("Utils")
@Description("Gives help about a command")
public final class HelpCommand extends TextCommand {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	private final BContext context;

	private final EmbedBuilder ownerHelpBuilder;
	private final Map<Long, Map<CommandPath, EmbedBuilder>> memberEmbedMap = new HashMap<>();

	public HelpCommand(BContext context) {
		this.context = context;
		this.ownerHelpBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());
	}

	public void generate() {
		fillGlobalHelp(ownerHelpBuilder, info -> true);
	}

	@NotNull
	private EmbedBuilder getCommandHelpEmbed(BaseCommandEvent event, TextCommandCandidates candidates) {
		final CommandPath path = candidates.first().getPath();

		final Member member = event.getMember();
		final long memberId = member.getIdLong();

		final Map<CommandPath, EmbedBuilder> map = memberEmbedMap.computeIfAbsent(memberId, x -> new HashMap<>());

		final EmbedBuilder builder;
		final EmbedBuilder existingEmbed = map.get(path);
		if (existingEmbed != null) {
			builder = new EmbedBuilder(existingEmbed);
		} else {
			builder = Utils.generateCommandHelp(candidates, event);

			map.put(path, builder);
		}

		builder.setTimestamp(Instant.now());
		builder.setColor(member.getColorRaw());

		return builder;
	}

	private void fillGlobalHelp(EmbedBuilder builder, Function<TextCommandInfo, Boolean> shouldAddFunc) {
		final TreeMap<String, StringJoiner> categoryBuilderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		final Set<String> insertedCommands = new HashSet<>();
		for (TextCommandCandidates cmds : ((BContextImpl) context).getCommands()) {
			final TextCommandInfo cmd = cmds.first();

			if (insertedCommands.add(cmd.getPath().getName())) {
				if (shouldAddFunc.apply(cmd)) {
					categoryBuilderMap
							.computeIfAbsent(Utils.getCategory(cmd), s -> new StringJoiner("\n"))
							.add("**" + cmd.getPath().getName() + "** : " + Utils.getNonBlankDescription(cmd));
				}
			}
		}

		for (Map.Entry<String, StringJoiner> entry : categoryBuilderMap.entrySet()) {
			builder.addField(entry.getKey(), entry.getValue().toString(), false);
		}

		final Consumer<EmbedBuilder> helpBuilderConsumer = context.getHelpBuilderConsumer();
		if (helpBuilderConsumer != null) helpBuilderConsumer.accept(builder);
	}

	@JDATextCommand(
			name = "help",
			description = "Gives help for all commands"
	)
	public void execute(CommandEvent event) {
		getAllHelp(event);
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

	private synchronized void getAllHelp(BaseCommandEvent event) {
		final EmbedBuilder builder = event.getContext().isOwner(event.getAuthor().getIdLong()) ? ownerHelpBuilder : getMemberGlobalHelpContent(event.getMember(), event.getChannel());

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

	private EmbedBuilder getMemberGlobalHelpContent(Member member, TextChannel channel) {
		final EmbedBuilder builder = context.getDefaultEmbedSupplier().get();

		fillGlobalHelp(builder, info -> Usability.of(info, member, channel, !context.isOwner(member.getIdLong())).isShowable());

		return builder;
	}

	public void sendCommandHelp(BaseCommandEvent event, CommandPath cmdPath) {
		TextCommandCandidates cmds = event.getContext().findCommands(cmdPath);
		if (cmds == null) {
			event.respond("Command '" + getSpacedPath(cmdPath) + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		final Member member = event.getMember();
		final TextChannel channel = event.getChannel();
		final Usability usability = Usability.of(cmds.first(), member, channel, !context.isOwner(member.getIdLong()));
		if (usability.isNotShowable()) {
			event.respond("Command '" + getSpacedPath(cmdPath) + "' does not exist").queue(null, event.failureReporter("Failed to send help"));
			return;
		}

		final EmbedBuilder embed = getCommandHelpEmbed(event, cmds);

		event.respond(embed.build()).queue();
	}

	@NotNull
	private String getSpacedPath(CommandPath cmdPath) {
		return cmdPath.toString().replace('/', ' ');
	}
}

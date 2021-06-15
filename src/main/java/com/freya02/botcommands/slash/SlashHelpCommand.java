package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Usability;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;

public final class SlashHelpCommand extends SlashCommand {
	private final BContext context;

	private final EmbedBuilder ownerHelpBuilder;
	private final MessageEmbed globalEmbed;

	public SlashHelpCommand(BContext context) {
		super(context);

		this.context = context;
		this.ownerHelpBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());

		final var globalBuilder = new EmbedBuilder(context.getDefaultEmbedSupplier().get());
		fillHelp(globalBuilder, info -> !info.isGuildOnly());

		if (globalBuilder.isEmpty()) globalBuilder.setDescription("No commands available");
		globalEmbed = globalBuilder.build();
	}

	public void generate() {
		fillHelp(ownerHelpBuilder, info -> true);
	}

	private void fillHelp(EmbedBuilder builder, Function<SlashCommandInfo, Boolean> shouldAddFunc) {
		TreeMap<String, StringJoiner> categoryBuilderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (SlashCommandInfo info : ((BContextImpl) context).getSlashCommands()) {
			if (shouldAddFunc.apply(info)) {
				categoryBuilderMap
						.computeIfAbsent(info.getCategory(), s -> new StringJoiner("\n"))
						.add("**/" + info.getPath().replace('/', ' ') + "** : " + info.getDescription());
			}
		}

		for (Map.Entry<String, StringJoiner> entry : categoryBuilderMap.entrySet()) {
			builder.addField(entry.getKey(), entry.getValue().toString(), false);
		}

		builder.setFooter("For a list of regular commands, use " + context.getPrefix() + "help");
	}

	@JdaSlashCommand(
			guildOnly = false,
			name = "help",
			description = "Gives help about a command",
			category = "Misc"
	)
	public void execute(SlashEvent event) {
		if (!event.isFromGuild()) {
			sendEmbed(event, globalEmbed);
		} else {
			final Member member = Objects.requireNonNull(event.getMember());

			final EmbedBuilder builder = context.isOwner(event.getUser().getIdLong()) ? ownerHelpBuilder : getMemberHelpContent(event);

			builder.setTimestamp(Instant.now());
			builder.setColor(member.getColorRaw());

			sendEmbed(event, builder.build());
		}
	}

	private void sendEmbed(SlashCommandEvent event, MessageEmbed embed) {
		event.deferReply(event.isFromGuild()).queue();

		final InputStream iconStream = context.getDefaultFooterIconSupplier().get();
		if (iconStream != null) {
			event.getHook().sendFile(iconStream, "icon.jpg").addEmbeds(embed).queue();
		} else {
			event.getHook().sendMessageEmbeds(embed).queue();
		}
	}

	private EmbedBuilder getMemberHelpContent(SlashCommandEvent event) {
		final EmbedBuilder builder = context.getDefaultEmbedSupplier().get();

		fillHelp(builder, info -> {
			final Member member = Objects.requireNonNull(event.getMember(), "Member shouldn't be null as this code path is guild-only");
			return Usability.of(event, info, !context.isOwner(member.getIdLong())).isShowable();
		});

		return builder;
	}
}
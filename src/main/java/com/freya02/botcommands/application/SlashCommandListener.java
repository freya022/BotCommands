package com.freya02.botcommands.application;

import com.freya02.botcommands.*;
import com.freya02.botcommands.Usability.UnusableReason;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;

public final class SlashCommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in slash command thread '" + t.getName() + "':", e));
		thread.setName("Slash command thread #" + commandThreadNumber++);

		return thread;
	});

	public SlashCommandListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onSlashCommand(@Nonnull SlashCommandEvent event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received slash command: {}", reconstructCommand(event));
		}

		runCommand(() -> {
			final SlashCommandInfo slashCommand = context.findSlashCommand(event.getCommandPath());

			if (slashCommand == null) {
				event.reply(context.getDefaultMessages().getSlashCommandNotFoundMsg()).queue();
				return;
			}

			final boolean isNotOwner = !context.isOwner(event.getUser().getIdLong());
			if (event.isFromGuild()) {
				final Usability usability = Usability.of(event, slashCommand, isNotOwner);

				if (usability.isUnusable()) {
					final var unusableReasons = usability.getUnusableReasons();
					if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
						reply(event, this.context.getDefaultMessages().getOwnerOnlyErrorMsg());
						return;
					} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
						reply(event, this.context.getDefaultMessages().getUserPermErrorMsg());
						return;
					} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
						final StringJoiner missingBuilder = new StringJoiner(", ");

						//Take needed permissions, extract bot current permissions
						final EnumSet<Permission> missingPerms = slashCommand.getBotPermissions();
						missingPerms.removeAll(
								Objects.requireNonNull(event.getGuild(), "Slash command's guild shouldn't be null as it is checked")
										.getSelfMember().getPermissions((GuildChannel) event.getChannel()));

						for (Permission botPermission : missingPerms) {
							missingBuilder.add(botPermission.getName());
						}

						reply(event, String.format(this.context.getDefaultMessages().getBotPermErrorMsg(), missingBuilder));
						return;
					}
				}
			}

			if (isNotOwner) {
				final int cooldown = slashCommand.getCooldown(event);
				if (cooldown > 0) {
					if (slashCommand.getCooldownScope() == CooldownScope.USER) {
						reply(event, String.format(this.context.getDefaultMessages().getUserCooldownMsg(), cooldown / 1000.0));
					} else if (slashCommand.getCooldownScope() == CooldownScope.GUILD) {
						reply(event, String.format(this.context.getDefaultMessages().getGuildCooldownMsg(), cooldown / 1000.0));
					} else { //Implicit channel
						reply(event, String.format(this.context.getDefaultMessages().getChannelCooldownMsg(), cooldown / 1000.0));
					}

					return;
				}
			}

			if (slashCommand.execute(context, event)) {
				slashCommand.applyCooldown(event);
			}
		}, event);
	}

	private void runCommand(RunnableEx code, SlashCommandEvent event) {
		commandService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				final String command = reconstructCommand(event);

				e = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing slash command '" + command + "'", e);
				if (event.isAcknowledged()) {
					event.getHook().sendMessage("An uncaught exception occurred").setEphemeral(true).queue();
				} else {
					event.reply(context.getDefaultMessages().getSlashCommandErrorMsg()).setEphemeral(true).queue();
				}

				context.dispatchException("Exception in slash command '" + command + "'", e);
			}
		});
	}

	@Nonnull
	public static String reconstructCommand(SlashCommandEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("/").append(event.getName());
		if (event.getSubcommandGroup() != null) sb.append(" ").append(event.getSubcommandGroup());
		if (event.getSubcommandName() != null) sb.append(" ").append(event.getSubcommandName());

		for (OptionMapping option : event.getOptions()) {
			sb.append(" ").append(option.getAsString());
		}

		return sb.toString();
	}

	private void reply(SlashCommandEvent event, String msg) {
		event.reply(msg)
				.setEphemeral(true)
				.queue(null,
						e -> {
							Utils.printExceptionString("Could not send reply message from slash command listener", e);

							context.dispatchException("Could not send reply message from slash command listener", e);
						}
				);
	}
}
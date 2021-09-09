package com.freya02.botcommands.application;

import com.freya02.botcommands.CooldownScope;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.RunnableEx;
import com.freya02.botcommands.internal.Usability;
import com.freya02.botcommands.internal.Usability.UnusableReason;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.commands.GenericCommandEvent;
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;

public final class ApplicationCommandListener extends ListenerAdapter {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;

	private int commandThreadNumber = 0;
	private final ExecutorService commandService = Utils.createCommandPool(r -> {
		final Thread thread = new Thread(r);
		thread.setDaemon(false);
		thread.setUncaughtExceptionHandler((t, e) -> Utils.printExceptionString("An unexpected exception happened in a context command thread '" + t.getName() + "':", e));
		thread.setName("Context command thread #" + commandThreadNumber++);

		return thread;
	});

	public ApplicationCommandListener(BContextImpl context) {
		this.context = context;
	}

	@Override
	public void onUserContextCommand(@NotNull UserContextCommandEvent event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received user command: {}", event.getName());
		}

		runCommand(() -> {
			final UserCommandInfo userCommand = context.findUserCommand(event.getCommandPath());

			if (userCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, userCommand)) return;

			if (userCommand.execute(context, event)) {
				userCommand.applyCooldown(event);
			}
		}, event);
	}

	@Override
	public void onMessageContextCommand(@NotNull MessageContextCommandEvent event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received message command: {}", event.getName());
		}

		runCommand(() -> {
			final MessageCommandInfo messageCommand = context.findMessageCommand(event.getCommandPath());

			if (messageCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, messageCommand)) return;

			if (messageCommand.execute(context, event)) {
				messageCommand.applyCooldown(event);
			}
		}, event);
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received slash command: {}", reconstructCommand(event));
		}

		runCommand(() -> {
			final SlashCommandInfo slashCommand = context.findSlashCommand(CommandPath.of(event.getCommandPath()));

			if (slashCommand == null) {
				event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandNotFoundMsg()).queue();
				return;
			}

			if (!canRun(event, slashCommand)) return;

			if (slashCommand.execute(context, event)) {
				slashCommand.applyCooldown(event);
			}
		}, event);
	}

	@NotNull
	public static String reconstructCommand(GenericCommandEvent event) {
		if (event instanceof SlashCommandEvent) {
			return ((SlashCommandEvent) event).getCommandString();
		} else {
			return "/" + event.getName();
		}
	}

	private boolean canRun(@NotNull GenericCommandEvent event, ApplicationCommandInfo applicationCommand) {
		final boolean isNotOwner = !context.isOwner(event.getUser().getIdLong());
		if (event.getGuild() != null) {
			final Usability usability = Usability.of(event, applicationCommand, isNotOwner);

			if (usability.isUnusable()) {
				final var unusableReasons = usability.getUnusableReasons();
				if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
					reply(event, this.context.getDefaultMessages(event.getGuild()).getOwnerOnlyErrorMsg());
					
					return false;
				} else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
					reply(event, this.context.getDefaultMessages(event.getGuild()).getUserPermErrorMsg());
					
					return false;
				} else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
					final StringJoiner missingBuilder = new StringJoiner(", ");

					//Take needed permissions, extract bot current permissions
					final EnumSet<Permission> missingPerms = applicationCommand.getBotPermissions();
					missingPerms.removeAll(event.getGuild().getSelfMember().getPermissions((GuildChannel) event.getChannel()));

					for (Permission botPermission : missingPerms) {
						missingBuilder.add(botPermission.getName());
					}

					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getBotPermErrorMsg(), missingBuilder));
					
					return false;
				}
			}
		}

		if (isNotOwner) {
			final int cooldown = applicationCommand.getCooldown(event, event::getName);
			if (cooldown > 0) {
				if (applicationCommand.getCooldownScope() == CooldownScope.USER) {
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getUserCooldownMsg(), cooldown / 1000.0));
				} else if (applicationCommand.getCooldownScope() == CooldownScope.GUILD) {
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getGuildCooldownMsg(), cooldown / 1000.0));
				} else { //Implicit channel
					reply(event, String.format(this.context.getDefaultMessages(event.getGuild()).getChannelCooldownMsg(), cooldown / 1000.0));
				}

				return false;
			}
		}
		
		return true;
	}

	private void runCommand(RunnableEx code, GenericCommandEvent event) {
		commandService.execute(() -> {
			try {
				code.run();
			} catch (Throwable e) {
				e = Utils.getException(e);

				Utils.printExceptionString("Unhandled exception in thread '" + Thread.currentThread().getName() + "' while executing an application command '" + reconstructCommand(event) + "'", e);
				if (event.isAcknowledged()) {
					event.getHook().sendMessage(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
				} else {
					event.reply(context.getDefaultMessages(event.getGuild()).getApplicationCommandErrorMsg()).setEphemeral(true).queue();
				}

				context.dispatchException("Exception in application command '" + reconstructCommand(event) + "'", e);
			}
		});
	}

	private void reply(Interaction event, String msg) {
		event.reply(msg)
				.setEphemeral(true)
				.queue(null,
						e -> {
							Utils.printExceptionString("Could not send reply message from application command listener", e);

							context.dispatchException("Could not send reply message from application command listener", e);
						}
				);
	}
}
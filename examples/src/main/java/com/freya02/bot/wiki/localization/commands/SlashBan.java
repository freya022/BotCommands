package com.freya02.bot.wiki.localization.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.localization.DefaultLocalizationTemplate;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;
import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

//TODO waiting on JDA localization changes (#2261)
/**
 * A few resources:
 *
 * <ul>
 *     <li>{@link DefaultLocalizationMapProvider DefaultLocalizationMapProvider - Where to put your localization file}</li>
 *     <li>{@link DefaultLocalizationTemplate DefaultLocalizationTemplate - How localization templates are formatted}</li>
 *     <li>{@link LocalizationFunction LocalizationFunction - How localization keys are made (from JDA)}</li>
 * </ul>
 */
@LocalizationBundle("LocalizationWikiCommands") //The file is in /resources/bc_localization/LocalizationWikiCommands.json
public class SlashBan extends ApplicationCommand {
	//Description is set in localization
	@JDASlashCommand(name = "ban")
	public void onSlashBan(GuildSlashEvent event,
	                       @AppOption(name = "user") User targetUser, //Description is set in localization
	                       @AppOption int delHours, //Description is set in localization
	                       @AppOption @Nullable String reason) { //Description is set in localization

		event.deferReply(true).queue();

		//Following localized strings will appear in the user's language, as it is provided for interactions (slash commands, context, buttons...)

		final Member caller = event.getMember();
		final Member botMember = event.getGuild().getSelfMember();

		//Check bot permissions
		if (!botMember.hasPermission(Permission.BAN_MEMBERS)) {
			final String localize = event.localize("ban.bot.permission_error");
			event.getHook().sendMessage(localize).queue();
			return;
		}

		//Check permissions & hierarchy
		// This requires the JDA chunking filter and member cache policy to be enabled, as this uses member cache.
		final Member targetMember = event.getGuild().getMember(targetUser);
		if (targetMember != null) { //Can be null if caching isn't set up
			if (!caller.canInteract(targetMember)) { //Check if the caller can interact with the banned member
				event.getHook()
						.sendMessage(event.localize("ban.caller.interact_error", entry("mention", targetMember.getAsMention())))
						.queue();
				return;
			} else if (!caller.hasPermission(Permission.BAN_MEMBERS)) { //Check if the caller can ban
				event.getHook()
						.sendMessage(event.localize("ban.caller.permission_error"))
						.queue();
				return;
			} else if (!botMember.canInteract(targetMember)) { //Check if the bot can interact with the banned member
				event.getHook()
						.sendMessage(event.localize("ban.bot.interact_error", entry("mention", targetMember.getAsMention())))
						.queue();
				return;
			}
		}

		final String notNullReason = Objects.requireNonNullElse(reason, "No reason");
		final String logReason = "Banned by " + caller.getAsMention() + " : " + notNullReason;

		//Ban user
		event.getGuild()
				.ban(targetUser, delHours, TimeUnit.HOURS)
				.reason(logReason)
				.queue(x -> {
					event.getHook().sendMessage(event.localize("ban.success",
							entry("banned_user", targetUser.getAsMention()),
							entry("del_hours", delHours),
							entry("reason", notNullReason)
					)).queue();
				}, t -> {
					event.getHook().sendMessage(event.localize("ban.unknown_error")).queue();
				});
	}
}

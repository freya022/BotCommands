package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.EnumSet;
import java.util.Objects;

import static com.freya02.botcommands.internal.Usability.UnusableReason.*;

public class Usability {
	private final EnumSet<UnusableReason> unusableReasons;

	private Usability(EnumSet<UnusableReason> unusableReasons) {
		this.unusableReasons = unusableReasons;
	}

	private static void checkNSFW(BContext context, EnumSet<UnusableReason> unusableReasons, MessageChannel msgChannel, AbstractCommandInfo<?> cmdInfo) {
		final NSFWState nsfwState = cmdInfo.getNSFWState();
		if (nsfwState == null) return;

		//The command is indeed marked NSFW, but where ?

		if (msgChannel instanceof TextChannel channel) {
			//If guild NSFW is not enabled, and we are in a guild channel
			if (!nsfwState.isEnabledInGuild()) {
				unusableReasons.add(NSFW_DISABLED);
			} else if (!channel.isNSFW()) { //If we are in a non-nsfw channel
				unusableReasons.add(NSFW_ONLY);
			}
		} else if (msgChannel instanceof PrivateChannel channel) {
			if (!nsfwState.isEnabledInDMs()) {
				unusableReasons.add(NSFW_DISABLED);
			} else {
				final SettingsProvider provider = context.getSettingsProvider();

				//If provider is null then assume there is no consent
				if (provider == null) {
					unusableReasons.add(NSFW_DM_DENIED);

					//If the user does not consent
				} else if (!provider.doesUserConsentNSFW(channel.getUser())) {
					unusableReasons.add(NSFW_DM_DENIED);
				}
			}
		}
	}

	public static Usability of(BContext context, TextCommandInfo cmdInfo, Member member, TextChannel channel, boolean isNotOwner) {
		final EnumSet<UnusableReason> unusableReasons = EnumSet.noneOf(UnusableReason.class);
		if (isNotOwner && cmdInfo.isHidden()) {
			unusableReasons.add(HIDDEN);
		}

		if (isNotOwner && cmdInfo.isOwnerRequired()) {
			unusableReasons.add(OWNER_ONLY);
		}

		checkNSFW(context, unusableReasons, channel, cmdInfo);

		if (isNotOwner && !member.hasPermission(channel, cmdInfo.getUserPermissions())) {
			unusableReasons.add(USER_PERMISSIONS);
		}

		if (!channel.getGuild().getSelfMember().hasPermission(channel, cmdInfo.getBotPermissions())) {
			unusableReasons.add(BOT_PERMISSIONS);
		}

		return new Usability(unusableReasons);
	}

	public static Usability of(BContext context, Interaction event, ApplicationCommandInfo cmdInfo, boolean isNotOwner) {
		final EnumSet<UnusableReason> unusableReasons = EnumSet.noneOf(UnusableReason.class);

		if (!event.isFromGuild() && cmdInfo.isGuildOnly()) {
			unusableReasons.add(GUILD_ONLY);
		}

		checkNSFW(context, unusableReasons, event.getMessageChannel(), cmdInfo);

		if (!event.isFromGuild()) {
			return new Usability(unusableReasons);
		}

		final TextChannel channel = event.getTextChannel();
		final Guild guild = Objects.requireNonNull(event.getGuild(), "Guild shouldn't be null as this code path is guild-only");
		final Member member = Objects.requireNonNull(event.getMember(), "Member shouldn't be null as this code path is guild-only");
		if (!guild.getSelfMember().hasPermission(channel, cmdInfo.getBotPermissions())) {
			unusableReasons.add(BOT_PERMISSIONS);
		}

		if (isNotOwner && cmdInfo.isOwnerOnly()) {
			unusableReasons.add(OWNER_ONLY);
		}

		if (isNotOwner && !member.hasPermission(channel, cmdInfo.getUserPermissions())) {
			unusableReasons.add(USER_PERMISSIONS);
		}

		return new Usability(unusableReasons);
	}

	public boolean isUnusable() {
		return unusableReasons.contains(HIDDEN) ||
				unusableReasons.contains(OWNER_ONLY) ||
				unusableReasons.contains(USER_PERMISSIONS) ||
				unusableReasons.contains(BOT_PERMISSIONS) ||
				unusableReasons.contains(GUILD_ONLY) ||
				unusableReasons.contains(NSFW_DISABLED) ||
				unusableReasons.contains(NSFW_ONLY) ||
				unusableReasons.contains(NSFW_DM_DENIED);
	}

	public boolean isUsable() {
		return !isUnusable();
	}

	public boolean isNotShowable() {
		return unusableReasons.contains(HIDDEN) ||
				unusableReasons.contains(OWNER_ONLY) ||
				unusableReasons.contains(USER_PERMISSIONS) ||
				unusableReasons.contains(GUILD_ONLY) ||
				unusableReasons.contains(NSFW_DISABLED) ||
				unusableReasons.contains(NSFW_ONLY) ||
				unusableReasons.contains(NSFW_DM_DENIED);
	}

	public boolean isShowable() {
		return !isNotShowable();
	}

	public EnumSet<UnusableReason> getUnusableReasons() {
		return unusableReasons;
	}

	//TODO put showable and usable as enum properties
	public enum UnusableReason {
		HIDDEN,
		OWNER_ONLY,
		USER_PERMISSIONS,
		BOT_PERMISSIONS,
		GUILD_ONLY,
		NSFW_DISABLED,
		NSFW_ONLY,
		NSFW_DM_DENIED
	}
}

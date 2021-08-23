package com.freya02.botcommands.internal;

import com.freya02.botcommands.application.ApplicationCommandInfo;
import com.freya02.botcommands.prefixed.CommandInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.EnumSet;
import java.util.Objects;

import static com.freya02.botcommands.internal.Usability.UnusableReason.*;

public class Usability {
	private final EnumSet<UnusableReason> unusableReasons;

	private Usability(EnumSet<UnusableReason> unusableReasons) {
		this.unusableReasons = unusableReasons;
	}

	public static Usability of(CommandInfo cmdInfo, Member member, TextChannel channel, boolean isNotOwner) {
		final EnumSet<UnusableReason> unusableReasons = EnumSet.noneOf(UnusableReason.class);
		if (isNotOwner && cmdInfo.isHidden()) {
			unusableReasons.add(HIDDEN);
		}

		if (isNotOwner && cmdInfo.isOwnerRequired()) {
			unusableReasons.add(OWNER_ONLY);
		}

		if (isNotOwner && !member.hasPermission(channel, cmdInfo.getUserPermissions())) {
			unusableReasons.add(USER_PERMISSIONS);
		}

		if (!channel.getGuild().getSelfMember().hasPermission(channel, cmdInfo.getBotPermissions())) {
			unusableReasons.add(BOT_PERMISSIONS);
		}

		return new Usability(unusableReasons);
	}

	public static Usability of(Interaction event, ApplicationCommandInfo cmdInfo, boolean isNotOwner) {
		final EnumSet<UnusableReason> unusableReasons = EnumSet.noneOf(UnusableReason.class);

		if (!event.isFromGuild() && cmdInfo.isGuildOnly()) {
			unusableReasons.add(GUILD_ONLY);
		}

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
				unusableReasons.contains(GUILD_ONLY);
	}

	public boolean isUsable() {
		return !isUnusable();
	}

	public boolean isNotShowable() {
		return unusableReasons.contains(HIDDEN) ||
				unusableReasons.contains(OWNER_ONLY) ||
				unusableReasons.contains(USER_PERMISSIONS) ||
				unusableReasons.contains(GUILD_ONLY);
	}

	public boolean isShowable() {
		return !isNotShowable();
	}

	public EnumSet<UnusableReason> getUnusableReasons() {
		return unusableReasons;
	}

	public enum UnusableReason {
		HIDDEN,
		OWNER_ONLY,
		USER_PERMISSIONS,
		BOT_PERMISSIONS,
		GUILD_ONLY
	}
}

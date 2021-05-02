package com.freya02.botcommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.EnumSet;

import static com.freya02.botcommands.Usability.UnusableReason.*;

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

	public boolean isUnusable() {
		return unusableReasons.contains(HIDDEN) ||
				unusableReasons.contains(OWNER_ONLY) ||
				unusableReasons.contains(USER_PERMISSIONS) ||
				unusableReasons.contains(BOT_PERMISSIONS);
	}

	public boolean isUsable() {
		return !isUnusable();
	}

	public boolean isNotShowable() {
		return unusableReasons.contains(HIDDEN) ||
				unusableReasons.contains(OWNER_ONLY) ||
				unusableReasons.contains(USER_PERMISSIONS);
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
		BOT_PERMISSIONS
	}
}

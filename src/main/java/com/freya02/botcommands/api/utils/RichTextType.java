package com.freya02.botcommands.api.utils;

import net.dv8tion.jda.api.entities.Message.MentionType;

import java.util.regex.Pattern;

public enum RichTextType {
	USER(MentionType.USER),
	EMOTE(MentionType.EMOTE),
	CHANNEL(MentionType.CHANNEL),
	ROLE(MentionType.ROLE),
	EVERYONE(MentionType.EVERYONE),
	HERE(MentionType.HERE),
	UNICODE_EMOTE(null),
	URL(null),
	TEXT(null);

	private final MentionType mentionType;
	RichTextType(MentionType mentionType) {
		this.mentionType = mentionType;
	}
	
	public MentionType getMentionType() {
		return mentionType;
	}
	
	public Pattern getPattern() {
		return mentionType.getPattern();
	}
}

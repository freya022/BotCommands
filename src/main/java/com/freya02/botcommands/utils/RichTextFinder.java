package com.freya02.botcommands.utils;

import com.vdurmont.emoji.EmojiParser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextFinder extends EmojiParser {
	private static final Pattern urlPattern = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private static final Pattern EMPTY_PATTERN = Pattern.compile("");

	private final String input;
	private final Matcher matcher;
	private final Map<Integer, RichText> normalMentionMap = new TreeMap<>();
	private final Map<Integer, String> addedStrs = new TreeMap<>();

	public RichTextFinder(String input, boolean getIMentionable, boolean getGlobalMentions, boolean getEmotes, boolean getUrls) {
		this.input = input;
		this.matcher = EMPTY_PATTERN.matcher(this.input);

		if (getIMentionable) {
			findAllMentions(RichTextType.USER, RichTextType.USER.getPattern());
			findAllMentions(RichTextType.CHANNEL, RichTextType.CHANNEL.getPattern());
			findAllMentions(RichTextType.EMOTE, RichTextType.EMOTE.getPattern());
			findAllMentions(RichTextType.ROLE, RichTextType.ROLE.getPattern());
		}

		if (getGlobalMentions) {
			findAllMentions(RichTextType.HERE, RichTextType.HERE.getPattern());
			findAllMentions(RichTextType.EVERYONE, RichTextType.EVERYONE.getPattern());
		}

		if (getEmotes) {
			resolveEmojis();
		}

		if (getUrls) {
			findAllMentions(RichTextType.URL, urlPattern);
		}

		if (!addedStrs.isEmpty()) {
			Iterator<String> it = addedStrs.values().iterator();
			String next = it.next();
			int startIndex = 0;
			int endIndex = input.indexOf(next, startIndex);
			final String startSubstring = this.input.substring(startIndex, endIndex).trim();
			if (!startSubstring.isEmpty()) {
				normalMentionMap.put(startIndex, new RichText(startSubstring, RichTextType.TEXT));
			}

			while (it.hasNext()) {
				startIndex = endIndex + next.length();

				next = it.next();
				endIndex = input.indexOf(next, startIndex);

				final String middleSubstring = this.input.substring(startIndex, endIndex).trim();
				if (!middleSubstring.isEmpty()) {
					normalMentionMap.put(startIndex, new RichText(middleSubstring, RichTextType.TEXT));
				}
			}

			startIndex = endIndex + next.length();
			final String endSubstring = this.input.substring(startIndex).trim();
			if (!endSubstring.isEmpty()) {
				normalMentionMap.put(startIndex, new RichText(endSubstring, RichTextType.TEXT));
			}

			addedStrs.clear();
		} else {
			normalMentionMap.put(0, new RichText(this.input, RichTextType.TEXT));
		}
	}

	public List<RichText> getResults() {
		return List.copyOf(normalMentionMap.values());
	}

	private void resolveEmojis() {
		//Find emoji aliases
		final int inputLength = input.length();
		for (int last = 0; last < inputLength; last++) {
			AliasCandidate alias = getAliasAt(input, last);

			if (alias != null) {
				last = alias.endIndex;

				final String substring;
				if (alias.fitzpatrick != null) {
					substring = alias.emoji.getUnicode() + alias.fitzpatrick.unicode;
				} else {
					substring = alias.emoji.getUnicode();
				}

				final int beginIndex = last;

				final boolean isInsideCustomEmote = normalMentionMap.values().stream().filter(r -> r.type == RichTextType.EMOTE).anyMatch(r -> {
					final List<String> aliases = alias.emoji.getAliases();
					for (String aliasItem : aliases) {
						final boolean customEmoteHasEmoji = r.substring.startsWith(':' + aliasItem + ':', 1);

						if (customEmoteHasEmoji) {
							return true;
						}
					}

					return false;
				});

				if (!isInsideCustomEmote) {
//					System.out.println("added emoji " + substring);
					normalMentionMap.put(beginIndex, new RichText(substring, RichTextType.UNICODE_EMOTE));
					addedStrs.put(beginIndex, input.substring(alias.startIndex, alias.endIndex + 1));
				}
			}
		}

		//Find unicode emojis
		char[] inputCharArray = input.toCharArray();
		UnicodeCandidate next;
		for (int i = 0; (next = getNextUnicodeCandidate(inputCharArray, i)) != null; i = next.getFitzpatrickEndIndex()) {
			if (next.hasFitzpatrick()) {
				normalMentionMap.put(next.getEmojiStartIndex(), new RichText(next.getEmoji().getUnicode(next.getFitzpatrick()), RichTextType.UNICODE_EMOTE));
				addedStrs.put(next.getEmojiStartIndex(), next.getEmoji().getUnicode(next.getFitzpatrick()));
			} else {
				normalMentionMap.put(next.getEmojiStartIndex(), new RichText(next.getEmoji().getUnicode(), RichTextType.UNICODE_EMOTE));
				addedStrs.put(next.getEmojiStartIndex(), next.getEmoji().getUnicode());
			}
		}
	}

	private void findAllMentions(RichTextType type, Pattern pattern) {
		matcher.usePattern(pattern);
		matcher.reset();
		while (matcher.find()) {
			final String group = matcher.group();
			normalMentionMap.put(matcher.start(), new RichText(group, type));
			addedStrs.put(matcher.start(), group);
		}
	}

	public void processResults(RichTextConsumer consumer) {
		for (RichText richText : normalMentionMap.values()) {
			consumer.consume(richText.substring, richText.type);
		}
	}

	public static class RichText {
		private final String substring;
		private final RichTextType type;

		RichText(String substring, RichTextType type) {
			this.substring = substring;
			this.type = type;
		}

		public String getSubstring() {
			return substring;
		}

		public RichTextType getType() {
			return type;
		}
	}
}

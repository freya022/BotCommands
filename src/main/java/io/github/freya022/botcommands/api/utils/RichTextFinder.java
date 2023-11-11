package io.github.freya022.botcommands.api.utils;

import io.github.freya022.botcommands.api.Logging;
import io.github.freya022.botcommands.internal.core.exceptions.InternalException;
import io.github.freya022.botcommands.internal.utils.ExceptionsKt;
import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to search for rich text.
 * <br>Rich text include:
 * <ul>
 *     <li>Users</li>
 *     <li>Text channels</li>
 *     <li>Emotes</li>
 *     <li>Roles</li>
 *     <li>Unicode/shortcode emojis</li>
 *     <li>{@code @here} and {@code @everyone} mentions</li>
 *     <li>URLs</li>
 * </ul>
 * <p>
 * This class takes your input and tokenises it as it finds what you're asking it to find.
 * <p>
 * You can then take the output using {@link #getResults()} or consume it directly using {@link #processResults(RichTextConsumer)}.
 */
public class RichTextFinder {
    private static final Logger LOGGER = Logging.getLogger();

    private static final Pattern urlPattern = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern EMPTY_PATTERN = Pattern.compile("");

    private static final Map<String, Emoji> emojiByDiscordAlias = new HashMap<>();

    static {
        for (Emoji emoji : EmojiManager.getAllEmojis()) {
            for (String discordAlias : emoji.getDiscordAliases()) {
                if (emojiByDiscordAlias.containsKey(discordAlias)) {
                    LOGGER.error("Existing alias: {}", discordAlias, new InternalException("Existing alias: " + discordAlias));
                    continue;
                }

                emojiByDiscordAlias.put(discordAlias, emoji);
            }
        }
    }

    private final String input;
    private final Matcher matcher;
    private final Map<Integer, RichText> normalMentionMap = new TreeMap<>();
    private final Map<Integer, String> addedStrs = new TreeMap<>();

    /**
     * Parses the input for what you're asking
     *
     * @param input             The input to parse
     * @param getIMentionable   Whether to take Users/Channels/Emotes/Roles
     * @param getGlobalMentions Whether to take {@code @here} and {@code @everyone} mentions
     * @param getEmojis         Whether to take Unicode/shortcode emojis
     * @param getUrls           Whether to take URLs
     */
    public RichTextFinder(String input, boolean getIMentionable, boolean getGlobalMentions, boolean getEmojis, boolean getUrls) {
        this.input = input.replace("\uFE0F", "");
        this.matcher = EMPTY_PATTERN.matcher(this.input);

        if (getIMentionable) {
            findAllMentions(RichTextType.USER, RichTextType.USER.getPattern());
            findAllMentions(RichTextType.CHANNEL, RichTextType.CHANNEL.getPattern());
            findAllMentions(RichTextType.EMOJI, RichTextType.EMOJI.getPattern());
            findAllMentions(RichTextType.ROLE, RichTextType.ROLE.getPattern());
        }

        if (getGlobalMentions) {
            findAllMentions(RichTextType.HERE, RichTextType.HERE.getPattern());
            findAllMentions(RichTextType.EVERYONE, RichTextType.EVERYONE.getPattern());
        }

        if (getEmojis) {
            extractAliasedEmojis();
            extractUnicodeEmojis();
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

    /**
     * @return The tokens parsed as rich text
     */
    public List<RichText> getResults() {
        return List.copyOf(normalMentionMap.values());
    }

    private void extractAliasedEmojis() {
        int aliasBegin = 0;
        while ((aliasBegin = input.indexOf(':', aliasBegin)) >= 0) {
            int aliasEnd = input.indexOf(':', aliasBegin + 2);  // Alias must be at least 1 char in length
            if (aliasEnd == -1) {
                aliasBegin += 1; // Do not find back the same alias
                continue; // No alias end found
            }

            final Emoji emoji = emojiByDiscordAlias.get(input.substring(aliasBegin, aliasEnd + 1));
            if (emoji == null) {
                aliasBegin += 1; // Do not find back the same alias
                continue;
            }

            if (!isInCustomEmote(emoji)) {
                normalMentionMap.put(aliasBegin, new RichText(emoji.getUnicode(), RichTextType.UNICODE_EMOTE));
                addedStrs.put(aliasBegin, input.substring(aliasBegin, aliasEnd + 1));
            }

            aliasBegin += 1; // Do not find back the same alias
        }
    }

    private boolean isInCustomEmote(Emoji emoji) {
        for (RichText richText : normalMentionMap.values()) {
            if (richText.type != RichTextType.EMOJI) continue;

            for (String aliasItem : emoji.getDiscordAliases()) {
                final boolean customEmoteHasEmoji = richText.substring.startsWith(aliasItem, 1);

                if (customEmoteHasEmoji) {
                    return true;
                }
            }
        }

        return false;
    }

    private void extractUnicodeEmojis() {
        final List<Emoji> emojis = EmojiManager.extractEmojisInOrder(input);
        int unicodeIndex = 0;
        for (Emoji emoji : emojis) {
            final int index = input.indexOf(emoji.getUnicode(), unicodeIndex);
            if (index <= unicodeIndex)
                ExceptionsKt.throwInternal("Found an unicode emoji at the same index as the start index");

            normalMentionMap.put(index, new RichText(emoji.getUnicode(), RichTextType.UNICODE_EMOTE));
            addedStrs.put(index, emoji.getUnicode());

            unicodeIndex = index + 1;
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

    /**
     * Processes each rich text token
     *
     * @param consumer The consumer accepting a substring and a rich text type
     */
    public void processResults(RichTextConsumer consumer) {
        for (RichText richText : normalMentionMap.values()) {
            consumer.consume(richText.substring, richText.type);
        }
    }

    public Map<Integer, RichText> getNormalMentionMap() {
        return normalMentionMap;
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

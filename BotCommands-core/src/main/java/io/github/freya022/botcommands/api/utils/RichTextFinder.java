package io.github.freya022.botcommands.api.utils;

import dev.freya02.jda.emojis.unicode.UnicodeEmojisManager;
import dev.freya02.jda.emojis.unicode.UnicodeEmojisManager.IndexedEmoji;
import net.dv8tion.jda.api.entities.Message.MentionType;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
 * This class takes your input and tokenizes it as it finds what you're asking it to find.
 * <p>
 * You can then take the output using {@link #getResults()} or consume it directly using {@link #processResults(RichTextConsumer)}.
 */
@NullMarked
public class RichTextFinder {
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern EXACT_CUSTOM_EMOJI_PATTERN = Pattern.compile("^<a?:[a-zA-Z0-9_]+:[0-9]+>");
    private static final Pattern EMPTY_PATTERN = Pattern.compile("");

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
            findAllMentions(RichTextType.USER, MentionType.USER.getPattern());
            findAllMentions(RichTextType.CHANNEL, MentionType.CHANNEL.getPattern());
            findAllMentions(RichTextType.EMOJI, MentionType.EMOJI.getPattern());
            findAllMentions(RichTextType.ROLE, MentionType.ROLE.getPattern());
        }

        if (getGlobalMentions) {
            findAllMentions(RichTextType.HERE, MentionType.HERE.getPattern());
            findAllMentions(RichTextType.EVERYONE, MentionType.EVERYONE.getPattern());
        }

        if (getEmojis) {
            extractAliasedEmojis();
            extractUnicodeEmojis();
        }

        if (getUrls) {
            findAllMentions(RichTextType.URL, URL_PATTERN);
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

                final String middleSubstring = this.input.substring(startIndex, endIndex);
                if (!middleSubstring.isEmpty()) {
                    normalMentionMap.put(startIndex, new RichText(middleSubstring, RichTextType.TEXT));
                }
            }

            startIndex = endIndex + next.length();
            final String endSubstring = this.input.substring(startIndex);
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

            final String alias = input.substring(aliasBegin + 1, aliasEnd);
            final String emoji = UnicodeEmojisManager.getEmojiByAlias(alias);
            if (emoji == null) {
                aliasBegin += 1; // Do not find back the same alias
                continue;
            }

            if (!isInCustomEmote(aliasBegin, aliasEnd)) {
                normalMentionMap.put(aliasBegin, new RichText(emoji, RichTextType.UNICODE_EMOTE));
                addedStrs.put(aliasBegin, input.substring(aliasBegin, aliasEnd + 1));
            }

            aliasBegin += 1; // Do not find back the same alias
        }
    }

    // This cannot be implemented by checking the ranges of found "EMOJI" substrings,
    // as the custom emoji parser may have been disabled
    private boolean isInCustomEmote(int aliasBegin, int aliasEnd) {
        int nearestChevron = input.lastIndexOf("<", aliasBegin);
        if (nearestChevron == -1) {
            // No custom emoji behind
            return false;
        }

        // Check if there is a custom emoji, and if there is, if the alias is included in the custom emoji's range
        Matcher matcher = EXACT_CUSTOM_EMOJI_PATTERN.matcher(input).region(nearestChevron, input.length());
        if (matcher.matches()) {
            // Is the alias inside the custom emoji?
            // No need to check the start bound, it is already set to the nearest '<'
            return aliasEnd < matcher.end();
        } else {
            // Not a custom emoji
            return false;
        }
    }

    private void extractUnicodeEmojis() {
        for (IndexedEmoji indexedEmoji : UnicodeEmojisManager.extractEmojiInOrder(input)) {
            normalMentionMap.put(indexedEmoji.getIndex(), new RichText(indexedEmoji.getSurrogates(), RichTextType.UNICODE_EMOTE));
            addedStrs.put(indexedEmoji.getIndex(), indexedEmoji.getSurrogates());
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

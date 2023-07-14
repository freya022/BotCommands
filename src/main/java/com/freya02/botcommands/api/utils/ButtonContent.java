package com.freya02.botcommands.api.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the visual content of a {@link Button}, this contains either an {@link Emoji} or a {@link String}
 */
public record ButtonContent(String text, Emoji emoji) {
	/**
	 * Constructs a {@link ButtonContent} from a {@link String}
	 *
	 * @param text The {@link String} to put in the {@link Button}
	 * @return The {@link ButtonContent} with the string
	 */
	public static ButtonContent withString(@NotNull String text) {
		return new ButtonContent(text, null);
	}

	/**
	 * Constructs a {@link ButtonContent} from an {@link Emoji}
	 *
	 * @param emoji The {@link Emoji} to put in the {@link Button}
	 * @return The {@link ButtonContent} with the emoji
	 */
	public static ButtonContent withEmoji(@NotNull Emoji emoji) {
		return new ButtonContent(null, emoji);
	}

	/**
	 * Constructs a {@link ButtonContent} from a {@link String} and an {@link Emoji}
	 *
	 * @param emoji The {@link Emoji} to put in the {@link Button}
	 * @return The {@link ButtonContent} with the text and emoji
	 */
	public static ButtonContent withEmoji(@NotNull String text, @NotNull Emoji emoji) {
		return new ButtonContent(text, emoji);
	}

	/**
	 * Constructs a {@link ButtonContent} from an unicode emoji
	 *
	 * @param unicode The unicode emoji
	 * @return The {@link ButtonContent} with the unicode emoji
	 */
	public static ButtonContent withEmoji(@NotNull String unicode) {
		return new ButtonContent(null, Emoji.fromUnicode(unicode));
	}

	/**
	 * Constructs a {@link ButtonContent} from a {@link String} and an unicode emoji
	 *
	 * @param unicode The unicode emoji
	 * @return The {@link ButtonContent} with the text and unicode emoji
	 */
	public static ButtonContent withEmoji(@NotNull String text, @NotNull String unicode) {
		return new ButtonContent(text, Emoji.fromUnicode(unicode));
	}

	/**
	 * Constructs a {@link ButtonContent} from a shortcode emoji, such as {@code :joy:}
	 *
	 * @param shortcode The shortcode emoji
	 * @return The {@link ButtonContent} with the shortcode emoji
	 */
	public static ButtonContent withShortcode(@NotNull String shortcode) {
		return new ButtonContent(null, EmojiUtils.resolveJDAEmoji(shortcode));
	}

	/**
	 * Constructs a {@link ButtonContent} from a {@link String} and a shortcode emoji, such as {@code :joy:}
	 *
	 * @param shortcode The shortcode emoji
	 * @return The {@link ButtonContent} with the text and shortcode emoji
	 */
	public static ButtonContent withShortcode(@NotNull String text, @NotNull String shortcode) {
		return new ButtonContent(text, EmojiUtils.resolveJDAEmoji(shortcode));
	}

	@Nullable
	public String text() {
		return text;
	}

	@Nullable
	public Emoji emoji() {
		return emoji;
	}
}

package com.freya02.botcommands.api.utils;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the visual content of a {@link Button}, this contains either an {@link Emoji} or a {@link String}
 */
public record ButtonContent(String str, Emoji emoji) {
	/**
	 * Constructs a {@link ButtonContent} from a {@link String}
	 *
	 * @param str The {@link String} to put in the {@link Button}
	 * @return The {@link ButtonContent} with the string
	 */
	public static ButtonContent withString(@NotNull String str) {
		return new ButtonContent(str, null);
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
	 * Constructs a {@link ButtonContent} from a unicode emoji
	 *
	 * @param unicode The unicode emoji
	 * @return The {@link ButtonContent} with the unicode emoji
	 */
	public static ButtonContent withEmoji(@NotNull String unicode) {
		return new ButtonContent(null, Emoji.fromUnicode(unicode));
	}

	/**
	 * Constructs a {@link ButtonContent} from a shortcode emoji, such as <code>:joy:</code>
	 *
	 * @param shortcode The shortcode emoji
	 * @return The {@link ButtonContent} with the shortcode emoji
	 */
	public static ButtonContent withShortcode(@NotNull String shortcode) {
		return new ButtonContent(null, EmojiUtils.resolveJDAEmoji(shortcode));
	}

	@Nullable
	public String str() {
		return str;
	}

	@Nullable
	public Emoji emoji() {
		return emoji;
	}
}

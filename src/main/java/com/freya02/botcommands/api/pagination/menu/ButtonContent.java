package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.builder.LambdaButtonBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the visual content of a {@link Button}, this contains either an {@link Emoji} or a {@link String}
 */
public class ButtonContent {
	private final String str;
	private final Emoji emoji;

	private ButtonContent(String str, Emoji emoji) {
		this.str = str;
		this.emoji = emoji;
	}

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
	 * Constructs a {@link ButtonContent} from a {@link String}
	 *
	 * @param emoji The {@link Emoji} to put in the {@link Button}
	 * @return The {@link ButtonContent} with the string
	 */
	public static ButtonContent withEmoji(@NotNull Emoji emoji) {
		return new ButtonContent(null, emoji);
	}

	public static Button applyContent(ButtonContent content, LambdaButtonBuilder buttonBuilder) {
		return content.getStr() != null
				? buttonBuilder.build(content.getStr())
				: buttonBuilder.build(content.getEmoji());
	}

	@Nullable
	public String getStr() {
		return str;
	}

	@Nullable
	public Emoji getEmoji() {
		return emoji;
	}
}

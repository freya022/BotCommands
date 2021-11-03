package com.freya02.botcommands.api.pagination.interactive;


import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SelectContent(@NotNull String label, @Nullable String description, @Nullable Emoji emoji) {
	@NotNull
	SelectOption toSelectOption(@NotNull String value) {
		return SelectOption.of(label, value).withDescription(description).withEmoji(emoji);
	}

	@NotNull
	public static SelectContent of(@NotNull String label) {
		return new SelectContent(label, null, null);
	}

	@NotNull
	public static SelectContent of(@NotNull String label, @Nullable String description) {
		return new SelectContent(label, description, null);
	}

	@NotNull
	public static SelectContent of(@NotNull String label, @Nullable String description, @Nullable Emoji emoji) {
		return new SelectContent(label, description, emoji);
	}
}

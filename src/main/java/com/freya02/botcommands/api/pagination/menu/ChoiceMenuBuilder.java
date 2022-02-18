package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.pagination.ButtonContentSupplier;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Builds a {@link ChoiceMenu}
 *
 * @param <E> Type of the entries
 */
public final class ChoiceMenuBuilder<E> extends BasicMenuBuilder<E, ChoiceMenuBuilder<E>, ChoiceMenu<E>> {
	private ChoiceCallback<E> callback;
	private ButtonContentSupplier<E> buttonContentSupplier;

	public ChoiceMenuBuilder(@NotNull List<E> entries) {
		super(entries);
	}

	/**
	 * Sets the callback for this menu
	 *
	 * @param callback The {@link ChoiceCallback} to call when the user makes their choice
	 * @return This builder for chaining convenience
	 */
	public ChoiceMenuBuilder<E> setCallback(@NotNull ChoiceCallback<E> callback) {
		this.callback = callback;

		return this;
	}

	/**
	 * Sets the button content supplier for this menu, allowing you to use custom buttons (text / emoji)
	 * <br>You get handed the object the button is bound to, as well as the object's index in the current page
	 * <br>So if you have a maximum of 5 items per page, your index is between 0 (included) and 5 (excluded)
	 *
	 * <br>
	 * <br>See limitations of buttons content at {@link Button#primary(String, String)} and {@link Button#primary(String, Emoji)}
	 *
	 * @param buttonContentSupplier The function which accepts an item of type <b>T</b> and an <b>item index</b> (local to the current page), and returns a {@link ButtonContent}
	 * @return This builder for chaining convenience
	 * @see ButtonContent#withString(String)
	 * @see ButtonContent#withEmoji(Emoji)
	 */
	public ChoiceMenuBuilder<E> setButtonContentSupplier(@NotNull ButtonContentSupplier<E> buttonContentSupplier) {
		this.buttonContentSupplier = buttonContentSupplier;

		return this;
	}

	@Override
	@NotNull
	public ChoiceMenu<E> build() {
		return new ChoiceMenu<>(constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, entries, maxEntriesPerPage, transformer, rowPrefixSupplier, paginatorSupplier, buttonContentSupplier, callback);
	}
}

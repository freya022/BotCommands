package com.freya02.botcommands.api.pagination.menu;

import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.ButtonContentSupplier;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.List;

/**
 * Provides a <b>choice</b> menu
 * <br>You provide the entries, it makes the pages for you, and also makes buttons, so you can choose an entry
 *
 * @param <E> Type of the entries
 * @see Paginator
 * @see Menu
 */
public final class ChoiceMenu<E> extends BasicMenu<E, ChoiceMenu<E>> {
	private final ButtonContentSupplier<E> buttonContentSupplier;
	private final ChoiceCallback<E> callback;

	ChoiceMenu(InteractionConstraints constraints,
	           TimeoutInfo<ChoiceMenu<E>> timeout,
	           boolean hasDeleteButton,
	           ButtonContent firstContent,
	           ButtonContent previousContent,
	           ButtonContent nextContent,
	           ButtonContent lastContent,
	           ButtonContent deleteContent,
	           List<E> entries,
	           int maxEntriesPerPage,
	           EntryTransformer<? super E> transformer,
	           RowPrefixSupplier rowPrefixSupplier,
	           PaginatorSupplier<ChoiceMenu<E>> supplier,
	           ButtonContentSupplier<E> buttonContentSupplier,
	           ChoiceCallback<E> callback) {
		super(constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
				makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
				supplier);

		Checks.notNull(buttonContentSupplier, "Button content supplier");
		Checks.notNull(callback, "Callback");

		this.buttonContentSupplier = buttonContentSupplier;
		this.callback = callback;
	}

	@Override
	protected void putComponents() {
		super.putComponents();

		final MenuPage<E> page = pages.get(this.page);
		final List<E> entries = page.entries();

		for (int i = 0; i < entries.size(); i++) {
			final E item = entries.get(i);
			final ButtonContent content = buttonContentSupplier.apply(item, i);
			final Button choiceButton = componentss.primaryButton(event -> {
				this.cleanup(event.getContext());

				callback.accept(event, item);
			}).setConstraints(constraints).build(content);

			components.addComponents(1 + (i / 5), choiceButton);
		}
	}
}

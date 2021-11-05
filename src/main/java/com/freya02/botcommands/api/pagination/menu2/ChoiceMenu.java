package com.freya02.botcommands.api.pagination.menu2;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.pagination.Paginator;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.pagination.menu.ButtonContent;
import com.freya02.botcommands.api.pagination.menu.ButtonContentSupplier;
import com.freya02.botcommands.api.pagination.transformer.EntryTransformer;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.List;
import java.util.function.BiConsumer;

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
	private final BiConsumer<ButtonEvent, E> callback;

	ChoiceMenu(long ownerId,
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
	           PaginatorSupplier supplier,
	           ButtonContentSupplier<E> buttonContentSupplier,
	           BiConsumer<ButtonEvent, E> callback) {
		super(ownerId, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
				makePages(entries, transformer, rowPrefixSupplier, maxEntriesPerPage),
				supplier);

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
			final Button choiceButton = Components.primaryButton(event -> {
				this.cleanup(event.getContext());

				callback.accept(event, item);
			}).ownerId(ownerId).build(content);

			components.addComponents(1 + (i / 5), choiceButton);
		}
	}
}

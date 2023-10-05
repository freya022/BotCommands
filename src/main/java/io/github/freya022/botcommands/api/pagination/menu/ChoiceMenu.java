package io.github.freya022.botcommands.api.pagination.menu;

import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;
import io.github.freya022.botcommands.api.pagination.ButtonContentSupplier;
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier;
import io.github.freya022.botcommands.api.pagination.TimeoutInfo;
import io.github.freya022.botcommands.api.pagination.paginator.Paginator;
import io.github.freya022.botcommands.api.pagination.transformer.EntryTransformer;
import io.github.freya022.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Paginator where pages are made from a list of entries, also adds buttons to choose an entry.
 *
 * @param <E> Type of the entries
 * @see Paginator
 * @see Menu
 */
public final class ChoiceMenu<E> extends BasicMenu<E, ChoiceMenu<E>> {
	private final ButtonContentSupplier<E> buttonContentSupplier;
	private final ChoiceCallback<E> callback;

	ChoiceMenu(@NotNull Components componentsService,
			   InteractionConstraints constraints,
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
		super(componentsService, constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent,
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
			final Button choiceButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, content, builder -> {
				builder.bindTo(event -> {
					this.cleanup();

					callback.accept(event, item);
				});
				builder.setConstraints(constraints);
			});

			components.addComponents(1 + (i / 5), choiceButton);
		}
	}
}

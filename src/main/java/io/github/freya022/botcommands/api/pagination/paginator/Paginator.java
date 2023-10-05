package io.github.freya022.botcommands.api.pagination.paginator;

import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;
import io.github.freya022.botcommands.api.pagination.PaginatorComponents;
import io.github.freya022.botcommands.api.pagination.PaginatorSupplier;
import io.github.freya022.botcommands.api.pagination.TimeoutInfo;
import io.github.freya022.botcommands.api.pagination.menu.Menu;
import io.github.freya022.botcommands.api.utils.ButtonContent;
import org.jetbrains.annotations.NotNull;

/**
 * Classic paginator, pages are supplied from {@link PaginatorBuilder#setPaginatorSupplier(PaginatorSupplier) paginator suppliers}.
 * <br>You provide the pages, it displays them one by one.
 * <br>Initial page is page 0, there is navigation buttons and an optional delete button
 * <br><b>The delete button cannot be used if the message is ephemeral</b>
 *
 * <p>
 * <b>The button IDs used by this paginator and those registered by the {@link PaginatorComponents} in the {@link PaginatorSupplier} are cleaned up once the embed is removed with the button</b>
 * <br>When the message is deleted, you would also have to call {@link #cleanup()}
 *
 * @see Menu
 */
public final class Paginator extends BasicPaginator<Paginator> {
	Paginator(@NotNull Components componentsService, InteractionConstraints constraints, TimeoutInfo<Paginator> timeout, int _maxPages, PaginatorSupplier<Paginator> supplier, boolean hasDeleteButton, ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent) {
		super(componentsService, constraints, timeout, _maxPages, supplier, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);
	}
}

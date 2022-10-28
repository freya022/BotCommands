package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.paginator.BasicPaginatorBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T> Type of the implementor
 * @param <R> Type of the implementor {@link #build()} return type
 */
@SuppressWarnings("unchecked")
public abstract class BasicInteractiveMenuBuilder<T extends BasicInteractiveMenuBuilder<T, R>, R extends BasicInteractiveMenu<R>> extends BasicPaginatorBuilder<T, R> {
	protected final List<InteractiveMenuItem<R>> items = new ArrayList<>();
	protected boolean usePaginator = false;

	/**
	 * Adds a menu to this {@link InteractiveMenu}
	 * <br><b>Note: The first added menu will be the first selected one</b>
	 *
	 * @param content  The content of the {@link SelectOption} bound to this menu
	 * @param supplier The interactive menu supplier for this menu's page
	 *
	 * @return This builder for chaining convenience
	 *
	 * @see SelectContent#of(String, String, Emoji)
	 */
	public T addMenu(@NotNull SelectContent content, int maxPages, @NotNull InteractiveMenuSupplier<R> supplier) {
		items.add(new InteractiveMenuItem<>(content, maxPages, supplier));

		return (T) this;
	}

	/**
	 * Sets whether the paginator buttons (previous, next, delete, etc...) should appear with this interactive menu
	 * <br>This is disabled by default
	 *
	 * @param usePaginator <code>true</code> to use the paginator buttons
	 *
	 * @return This builder for chaining convenience
	 */
	public T usePaginator(boolean usePaginator) {
		this.usePaginator = usePaginator;

		return (T) this;
	}
}

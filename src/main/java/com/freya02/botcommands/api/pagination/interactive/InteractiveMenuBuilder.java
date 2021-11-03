package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.BasicPaginationBuilder;
import com.freya02.botcommands.api.pagination.PaginationSupplier;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InteractiveMenuBuilder extends BasicPaginationBuilder<InteractiveMenuBuilder, InteractiveMenu> {
	private final List<InteractiveMenuItem> items = new ArrayList<>();

	/**
	 * Adds a menu to this {@link InteractiveMenu}
	 * <br><b>Note: The first added menu will be the first selected one</b>
	 *
	 * @param content  The content of the {@link SelectOption} bound to this menu
	 * @param supplier The interactive menu supplier for this menu's page
	 * @return This builder for chaining convenience
	 * @see SelectContent#of(String, String, Emoji)
	 */
	public InteractiveMenuBuilder addMenu(@NotNull SelectContent content, @NotNull PaginationSupplier supplier) {
		items.add(new InteractiveMenuItem(content, supplier));

		return this;
	}

	@Override
	public InteractiveMenu build() {
		return new InteractiveMenu(items, ownerId, timeout);
	}
}

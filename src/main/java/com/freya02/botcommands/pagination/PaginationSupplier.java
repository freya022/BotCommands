package com.freya02.botcommands.pagination;

import com.freya02.botcommands.pagination.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Used to provide pages for {@link Paginator} or {@link Menu}, you get an {@link EmbedBuilder}, you fill it so they can use it. <br>
 * You can also add more buttons with {@link PaginatorComponents}
 */
public interface PaginationSupplier {
	void accept(EmbedBuilder builder, PaginatorComponents components, int page);
}

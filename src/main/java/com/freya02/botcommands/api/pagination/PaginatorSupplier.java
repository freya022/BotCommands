package com.freya02.botcommands.api.pagination;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @param <T> Type of the paginator instance
 *
 * @see #get(Object, MessageEditBuilder, PaginatorComponents, int)
 */
public interface PaginatorSupplier<T> {
	/**
	 * Returns the {@link MessageEmbed} for this paginator page
	 *
	 * @param paginator   The paginator instance this pagination supplier is for, for example this may allow you to:
	 *                    <ul>
	 *                       <li>Modify the pagination's state when a button is triggered</li>
	 *                       <li>Delete the pagination, cancel the timeout and cleanup the components when a button is clicked</li>
	 *                    </ul>
	 * @param editBuilder The {@link MessageEditBuilder} for this interactive menu, you can mostly ignore it but can use it to add attachments for examples, to use them in your embeds
	 * @param components  The {@link PaginatorComponents} for this interactive menu's page, this allows you to add components on this page
	 *                    <br><b>Do not use {@link MessageEditBuilder#setComponents(LayoutComponent...)} and such, these will be overridden by the menu</b>
	 *
	 * @return A {@link MessageEmbed} for this interactive menu's page
	 */
	@NotNull
	MessageEmbed get(@NotNull T paginator, @NotNull MessageEditBuilder editBuilder, @NotNull PaginatorComponents components, int page);
}

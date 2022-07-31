package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.PaginatorComponents;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @see #get(BasicInteractiveMenu, MessageEditBuilder, PaginatorComponents)
 */
public interface InteractiveMenuSupplier<T extends BasicInteractiveMenu<T>> {
	/**
	 * Returns the {@link MessageEmbed} for this interactive menu's page
	 *
	 * @param interactiveMenu The interaction menu instance this interactive menu supplier is for, for example this may allow you to:
	 *                        <ul>
	 *                          <li>Modify the interactive menu's state when a button is triggered</li>
	 *                          <li>Delete the menu, cancel the timeout and cleanup the components when a button is clicked</li>
	 *                        </ul>
	 * @param messageBuilder  The {@link MessageEditBuilder} for this interactive menu, you can mostly ignore it but can use it to add attachments for examples, to use them in your embeds
	 * @param components      The {@link PaginatorComponents} for this interactive menu's page, this allows you to add components on this page
	 *                        <br><b>Do not use {@link MessageEditBuilder#setComponents(LayoutComponent...)} and such, these will be overridden by the menu</b>
	 *
	 * @return A {@link MessageEmbed} for this interactive menu's page
	 */
	@NotNull
	MessageEmbed get(@NotNull T interactiveMenu, @NotNull MessageEditBuilder messageBuilder, @NotNull PaginatorComponents components);
}

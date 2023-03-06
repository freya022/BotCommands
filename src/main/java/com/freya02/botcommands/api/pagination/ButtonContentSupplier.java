package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * A {@link ButtonContent} supplier for use in different paginators, allowing you to use custom buttons (text / emoji).
 * <br>You get handed the object the button is bound to, as well as the object's index in the current page.
 * <br>So for a menu, if you have a maximum of 5 items per page, your index is between 0 (included) and 5 (excluded).
 *
 * <br>
 * <br>See limitations of buttons content at {@link Button#primary(String, String)} and {@link Button#primary(String, Emoji)}.
 *
 * @param <T> Item type
 * @see java.util.function.BiFunction
 * @see ButtonContent#withString(String)
 * @see ButtonContent#withEmoji(Emoji)
 */
public interface ButtonContentSupplier<T> {
	/**
	 * Returns a {@link ButtonContent} based on the given item and the current page number of the paginator
	 *
	 * @param item  The item bound to this button
	 * @param index The index of this item on the current page number of the paginator
	 * @return The {@link ButtonContent} of this item
	 */
	ButtonContent apply(T item, int index);
}

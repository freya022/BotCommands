package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A type of pagination which shows embeds and provides a {@link SelectMenu} to navigate between menus
 * <br>Each embed is bound to a selection menu
 * <br><i>This does not provide pagination for each embed</i> (no arrow buttons, only the selection menu)
 */
public final class InteractiveMenu extends BasicInteractiveMenu<InteractiveMenu> {
	InteractiveMenu(InteractionConstraints constraints, TimeoutInfo<InteractiveMenu> timeout, boolean hasDeleteButton,
	                ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent,
	                @NotNull List<InteractiveMenuItem<InteractiveMenu>> items) {
		super(constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, items);
	}
}

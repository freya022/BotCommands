package io.github.freya022.botcommands.api.pagination.interactive;

import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;
import io.github.freya022.botcommands.api.pagination.TimeoutInfo;
import io.github.freya022.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A type of pagination which shows embeds and provides a {@link StringSelectMenu} to navigate between menus.
 * <br>Each embed is bound to a selection menu.
 * <br><i>This does not provide pagination for each embed</i> (no arrow buttons, only the selection menu).
 */
public final class InteractiveMenu extends BasicInteractiveMenu<InteractiveMenu> {
	InteractiveMenu(@NotNull Components componentsService,
					InteractionConstraints constraints, TimeoutInfo<InteractiveMenu> timeout, boolean hasDeleteButton,
					ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent,
					@NotNull List<InteractiveMenuItem<InteractiveMenu>> items, boolean usePaginator) {
		super(componentsService, constraints, timeout, hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent, items, usePaginator);
	}
}

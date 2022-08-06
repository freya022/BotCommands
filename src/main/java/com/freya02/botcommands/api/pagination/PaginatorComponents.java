package com.freya02.botcommands.api.pagination;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PaginatorComponents {
	private final List<List<ActionComponent>> actionRows = new ArrayList<>(5);

	public void addComponents(int row, ActionComponent... components) {
		if (row >= 5) throw new IllegalArgumentException("Cannot have more than 5 rows");

		if (actionRows.size() <= row) {
			actionRows.add(new ArrayList<>(Arrays.asList(components)));
		} else {
			final List<ActionComponent> list = actionRows.get(row);
			if (list.size() + components.length > 5)
				throw new UnsupportedOperationException("Cannot put more than 5 buttons in row " + row + " (contains " + list.size() + ")");

			Collections.addAll(list, components);
		}
	}

	/**
	 * Adds components on a new row
	 * <br>You cannot add more than 5 rows nor add more than 5 components on the same row
	 *
	 * @param components The components to add
	 */
	public void addComponents(ActionComponent... components) {
		Checks.check(actionRows.size() < 5, "There are already 5 action rows");
		Checks.check(components.length <= 5, "You cannot add more than 5 components in the same row");

		actionRows.add(Arrays.asList(components));
	}

	public List<ActionRow> getActionRows() {
		return actionRows.stream().map(ActionRow::of).toList();
	}

	public void clear() {
		actionRows.clear();
	}
}

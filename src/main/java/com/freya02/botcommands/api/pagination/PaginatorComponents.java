package com.freya02.botcommands.api.pagination;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaginatorComponents {
	private final List<ActionRow> actionRows = new ArrayList<>(5);

	public PaginatorComponents() { }

	public PaginatorComponents(List<ActionRow> components) {
		actionRows.addAll(components);
	}

	public void addComponents(int row, ActionComponent... components) {
		if (row >= 5) throw new IllegalArgumentException("Cannot have more than 5 rows");

		if (actionRows.size() <= row) {
			actionRows.add(ActionRow.of(components));
		} else {
			final List<ActionComponent> list = actionRows.get(row).getComponents();
			if (list.size() + components.length > 5)
				throw new UnsupportedOperationException("Cannot put more than 5 buttons in row " + row + " (contains " + list.size() + ")");

			Collections.addAll(list, components);
		}
	}

	public List<ActionRow> getActionRows() {
		return actionRows;
	}

	public void clear() {
		actionRows.clear();
	}
}

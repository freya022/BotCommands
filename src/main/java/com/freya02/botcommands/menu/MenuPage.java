package com.freya02.botcommands.menu;

import java.util.List;

public class MenuPage<T> {
	private final String description;
	private final List<T> choices;

	MenuPage(String description, List<T> choices) {
		this.description = description;
		this.choices = choices;
	}

	String getDescription() {
		return description;
	}

	List<T> getChoices() {
		return choices;
	}
}

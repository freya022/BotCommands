package com.freya02.botcommands.api.application;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.ArrayList;

/**
 * Utility class to generate a list of empty choices meant to be localized later
 */
public class ChoiceList extends ArrayList<Command.Choice> {
	private ChoiceList(int size) {
		for (int i = 0; i < size; i++) {
			add(new Command.Choice("", ""));
		}
	}

	/**
	 * Convenience method to create a list of <code>size</code> empty choices, which are meant to be localized via deduced localization labels (such as <code>slash.methodName.options.optionIndex.choices.choiceIndex</code>)
	 *
	 * @param size The number of choices to insert
	 */
	public static ChoiceList ofSize(int size) {
		return new ChoiceList(size);
	}
}

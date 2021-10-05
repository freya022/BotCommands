package com.freya02.botcommands.internal.prefixed;

import org.jetbrains.annotations.NotNull;

import java.util.TreeSet;

public class TextCommandCandidates extends TreeSet<TextCommandInfo> {
	//A list of text commands must not be empty
	public TextCommandCandidates(TextCommandInfo commandInfo) {
		super(new TextCommandComparator());

		add(commandInfo);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@NotNull
	public TextCommandInfo findFirst() {
		return first();
	}
}
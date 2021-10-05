package com.freya02.botcommands.internal.prefixed;

import java.util.ArrayList;

public class TextSubcommandCandidates extends ArrayList<TextCommandCandidates> {
	public TextSubcommandCandidates(TextCommandInfo commandInfo) {
		add(new TextCommandCandidates(commandInfo));
	}

	public TextSubcommandCandidates addSubcommand(TextCommandInfo commandInfo) {
		//We have to insert the subcommands into the right List<TextCommandInfo>
		// So we iterate all of them and check if the first info has a path that's the same as ours
		// If we find a path, we add it; if not, we create the list and insert it into the upper list
		for (TextCommandCandidates candidates : this) {
			final TextCommandInfo first = candidates.findFirst();

			if (first.getPath().equals(commandInfo.getPath())) {
				candidates.add(commandInfo);

				return this;
			}
		}

		throw new IllegalStateException("Tried to add a subcommand to an incompatible subcommand group");
	}
}
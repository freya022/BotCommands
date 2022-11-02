package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import net.dv8tion.jda.api.interactions.commands.Command;

public interface AutocompleteTransformer<E> {
	Command.Choice apply(E e);
}

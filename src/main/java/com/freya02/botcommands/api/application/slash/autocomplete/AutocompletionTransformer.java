package com.freya02.botcommands.api.application.slash.autocomplete;

import net.dv8tion.jda.api.interactions.commands.Command;

public interface AutocompletionTransformer<E> {
	Command.Choice apply(E e);
}

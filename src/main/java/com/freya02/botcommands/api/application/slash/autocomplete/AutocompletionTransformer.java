package com.freya02.botcommands.api.application.slash.autocomplete;

import net.dv8tion.jda.api.interactions.commands.SlashCommand;

public interface AutocompletionTransformer<E> {
	SlashCommand.Choice apply(E e);
}

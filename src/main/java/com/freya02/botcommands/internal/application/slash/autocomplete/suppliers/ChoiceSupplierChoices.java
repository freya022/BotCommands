package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers;

import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.List;

public class ChoiceSupplierChoices implements ChoiceSupplier {
	private final AutocompletionHandlerInfo handlerInfo;

	public ChoiceSupplierChoices(AutocompletionHandlerInfo handlerInfo) {
		this.handlerInfo = handlerInfo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Command.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception {
		final Collection<Command.Choice> choices = (Collection<Command.Choice>) collection;

		return choices.stream()
				.limit(handlerInfo.getMaxChoices())
				.toList();
	}
}

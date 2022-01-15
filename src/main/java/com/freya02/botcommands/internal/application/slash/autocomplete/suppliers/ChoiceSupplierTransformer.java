package com.freya02.botcommands.internal.application.slash.autocomplete.suppliers;

import com.freya02.botcommands.api.application.slash.autocomplete.AutocompletionTransformer;
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompletionHandlerInfo;
import com.freya02.botcommands.internal.application.slash.autocomplete.ChoiceSupplier;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ChoiceSupplierTransformer implements ChoiceSupplier {
	private final AutocompletionHandlerInfo handlerInfo;
	private final AutocompletionTransformer<Object> transformer;

	public ChoiceSupplierTransformer(AutocompletionHandlerInfo handlerInfo, AutocompletionTransformer<Object> transformer) {
		this.handlerInfo = handlerInfo;
		this.transformer = transformer;
	}

	@Override
	public List<Command.Choice> apply(CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception {
		return collection.stream()
				.limit(handlerInfo.getMaxChoices())
				.map(transformer::apply)
				.collect(Collectors.toList());
	}
}

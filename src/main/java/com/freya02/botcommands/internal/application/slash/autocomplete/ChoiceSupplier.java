package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.List;

public interface ChoiceSupplier {
	List<Command.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteInteractionEvent event, Collection<?> collection) throws Exception;
}

package com.freya02.botcommands.internal.application.slash.autocomplete;

import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.events.interaction.CommandAutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;

import java.util.List;

public interface ChoiceSupplier {
	List<SlashCommand.Choice> apply(SlashCommandInfo slashCommand, CommandAutoCompleteEvent event) throws Exception;
}

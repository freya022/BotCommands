package com.freya02.bot.componentsbot.commands;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JdaButtonListener;
import com.freya02.botcommands.api.components.annotations.JdaSelectionMenuListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.SelectionEvent;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JdaTextCommand;
import net.dv8tion.jda.api.interactions.components.ActionRow;

@Description("Shows how components works")
public class MessageComponents extends TextCommand {
	private static final String BUTTON_HANDLER_NAME = "normalButtonHandler";
	private static final String SELECTION_HANDLER_NAME = "normalSelectionHandler";

	@JdaTextCommand(name = "components")
	public void execute(CommandEvent event) {
		final ActionRow firstRow = ActionRow.of(
				Components.selectionMenu(SELECTION_HANDLER_NAME)
						.addOption("Option 1", "Value 1")
						.addOption("Option 2", "Value 2")
						.addOption("Option 3", "Value 3")
						.setPlaceholder("Select a value")
						.build()
		);

		final ActionRow secondRow = ActionRow.of(
				Components.primaryButton(BUTTON_HANDLER_NAME).build("Primary button (persistent, no group)")
		);

		event.getMessage().reply("Components !")
				.setActionRows(firstRow, secondRow)
				.queue();
	}

	@JdaButtonListener(name = BUTTON_HANDLER_NAME)
	public void run(ButtonEvent event) {
		event.reply("Primary button clicked ! You should be able to click it again as it is not grouped nor one-use.")
				.setEphemeral(true)
				.queue();
	}

	@JdaSelectionMenuListener(name = SELECTION_HANDLER_NAME)
	public void run(SelectionEvent event) {
		event.reply("Selected a value in a persistent selection menu: " + event.getValues())
				.setEphemeral(true)
				.queue();
	}
}

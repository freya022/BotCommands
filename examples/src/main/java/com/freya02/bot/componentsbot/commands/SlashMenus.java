package com.freya02.bot.componentsbot.commands;

import com.freya02.botcommands.annotation.CommandMarker;
import com.freya02.botcommands.components.Components;
import com.freya02.botcommands.components.annotation.JdaSelectionMenuListener;
import com.freya02.botcommands.components.event.SelectionEvent;
import com.freya02.botcommands.slash.SlashCommand;
import com.freya02.botcommands.slash.SlashEvent;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;

@CommandMarker
public class SlashMenus extends SlashCommand {
	private static final String SELECTION_HANDLER_NAME = "selectionHandler";

	@JdaSlashCommand(
			guildOnly = false,
			name = "menus",
			description = "Shows how menus works"
	)
	public void run(SlashEvent event) {
		event.reply("Selection menus !")
				//A persistent selection menu, still works after a bot restarts
				.addActionRow(Components.selectionMenu(SELECTION_HANDLER_NAME)
						.addOption("Option 1", "Value 1")
						.addOption("Option 2", "Value 2")
						.addOption("Option 3", "Value 3")
						.setPlaceholder("Select a value")
						.build())
				//A lambda selection menu, won't work after a bot restart
				.addActionRow(Components.selectionMenu(e -> e.reply("Selected a value in a lambda selection menu: " + e.getValues()).setEphemeral(true).queue())
						.addOption("Option 1", "Value 1")
						.addOption("Option 2", "Value 2")
						.addOption("Option 3", "Value 3")
						.setPlaceholder("Select a value")
						.build())
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

package com.freya02.bot.componentsbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

@CommandMarker //Just so the class isn't marked as unused
public class SlashMenus extends ApplicationCommand {
	private static final String SELECTION_HANDLER_NAME = "selectionHandler";

	@JDASlashCommand(
			scope = CommandScope.GLOBAL,
			name = "menus",
			description = "Shows how menus works"
	)
	public void run(GlobalSlashEvent event) {
		event.reply("Selection menus !")
				//A persistent selection menu, still works after a bot restarts
				.addActionRow(Components.stringSelectionMenu(SELECTION_HANDLER_NAME)
						.addOption("Option 1", "Value 1")
						.addOption("Option 2", "Value 2")
						.addOption("Option 3", "Value 3")
						.setPlaceholder("Select a value")
						.build())
				//A lambda selection menu, won't work after a bot restart
				.addActionRow(Components.stringSelectionMenu(e -> e.reply("Selected a value in a lambda selection menu: " + e.getValues()).setEphemeral(true).queue())
						.addOption("Option 1", "Value 1")
						.addOption("Option 2", "Value 2")
						.addOption("Option 3", "Value 3")
						.setPlaceholder("Select a value")
						.build())
				.setEphemeral(true)
				.queue();
	}

	@JDASelectionMenuListener(name = SELECTION_HANDLER_NAME)
	public void run(StringSelectionEvent event) {
		event.reply("Selected a value in a persistent selection menu: " + event.getValues())
				.setEphemeral(true)
				.queue();
	}
}

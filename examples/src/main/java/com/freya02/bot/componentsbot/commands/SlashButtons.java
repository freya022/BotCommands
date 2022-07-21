package com.freya02.bot.componentsbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CommandMarker //Just so the class isn't marked as unused
public class SlashButtons extends ApplicationCommand {
	private static final String PRIMARY_HANDLER_NAME = "primaryHandler";

	@JDASlashCommand(
			scope = CommandScope.GLOBAL,
			name = "buttons",
			description = "Shows how buttons works"
	)
	public void run(GlobalSlashEvent event) {
		List<ActionComponent> components = new ArrayList<>();
		Collections.addAll(components, Components.group(
				//A persistent button, works after a bot restart
				Components.primaryButton(PRIMARY_HANDLER_NAME).build("Primary (Persistent, group 1)"),
				//A lambda button, does not work after a bot restart
				Components.dangerButton(e -> event.reply("Danger button clicked ! You should not be able to click it again as it is grouped.")
						.setEphemeral(true)
						.queue()).build("Danger (Lambda, group 1)")
		));
		components.add(
				Components.secondaryButton(e -> e.reply("Secondary button clicked ! You should be able to click it as it is not grouped nor one-use.")
						.setEphemeral(true)
						.queue()).build("Secondary (Lambda, no group)")
		);

		event.reply("Buttons !")
				.addActionRow(components)
				.setEphemeral(true)
				.queue();
	}

	@JDAButtonListener(name = PRIMARY_HANDLER_NAME)
	public void run(ButtonEvent event) {
		event.reply("Primary button clicked ! You should not be able to click it again as it is grouped.")
				.setEphemeral(true)
				.queue();
	}
}

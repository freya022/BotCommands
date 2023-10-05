package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

@Dependencies(Components.class)
public class SlashButton extends ApplicationCommand {
	private static final String PERSISTENT_BUTTON_LISTENER_NAME = "leBouton";

	@JDASlashCommand(name = "button")
	public void onSlashButton(GuildSlashEvent event, Components components) {
		event.reply("Buttons")
				.addActionRow(
						components.persistentButton(ButtonStyle.PRIMARY, "Persistent button (1 minute timeout)", builder -> {
							builder.setOneUse(true);
							builder.bindTo(PERSISTENT_BUTTON_LISTENER_NAME, System.currentTimeMillis());
							builder.timeout(1, TimeUnit.MINUTES);
						}),
						components.ephemeralButton(ButtonStyle.PRIMARY, "Ephemeral button (1 second timeout)", builder -> {
							builder.bindTo(btnEvt -> btnEvt.deferEdit().queue());
							builder.timeout(1, TimeUnit.SECONDS, () -> event.getHook().editOriginal("Ephemeral expired :/").queue());
						})
				)
				.setEphemeral(true)
				.queue();
	}

	@JDAButtonListener(name = PERSISTENT_BUTTON_LISTENER_NAME)
	public void onPersistentButtonClicked(ButtonEvent event, @SlashOption long timeCreated, JDA jda) {
		event.replyFormat("Button created on %s and I am %s", timeCreated, jda.getSelfUser().getAsTag())
				.setEphemeral(true)
				.queue();
	}
}

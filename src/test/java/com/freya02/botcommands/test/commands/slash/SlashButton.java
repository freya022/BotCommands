package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.TimeUnit;

public class SlashButton extends ApplicationCommand {
	private static final String HANDLER_NAME = "leBouton";

	public static class InnerHandler { //just to prove buttons don't need ApplicationCommand
		@JDAButtonListener(name = HANDLER_NAME)
		public void leBouton(ButtonEvent event, @AppOption long xd, JDA jda) {
			event.reply("Le bouton c le bouton " + xd + " : " + jda)
					.setEphemeral(true)
					.queue();
		}
	}

	@JDASlashCommand(name = "button")
	public void button(GuildSlashEvent event) {
		event.reply("Button")
				.addActionRow(
						Components.primaryButton(HANDLER_NAME, System.currentTimeMillis())
								.oneUse()
								.timeout(1, TimeUnit.MINUTES)
								.build("Le bouton"),
						Components.primaryButton(btnEvt -> {
									btnEvt.deferEdit().queue();
								})
								.oneUse()
								.timeout(1, TimeUnit.SECONDS, () -> {
									event.getHook().editOriginal("big oof").queue();
								})
								.build("Le bouton 2")
				)
				.setEphemeral(true)
				.queue();
	}
}

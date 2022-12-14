package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.concurrent.TimeUnit;

public class SlashButton extends ApplicationCommand {
	private static final String HANDLER_NAME = "leBouton";

	@JDASlashCommand(name = "button")
	public void button(GuildSlashEvent event, Components components) {
		event.reply("Button")
				.addActionRow(
						components.persistentButton(ButtonStyle.PRIMARY, "Le bouton", builder -> {
							builder.setOneUse(true);
							builder.bindTo(HANDLER_NAME, System.currentTimeMillis());
							builder.timeout(1, TimeUnit.MINUTES);
						}),
						components.ephemeralButton(ButtonStyle.PRIMARY, "Le bouton 2", builder -> {
							builder.bindTo(btnEvt -> btnEvt.deferEdit().queue());
							builder.timeout(1, TimeUnit.SECONDS, () -> event.getHook().editOriginal("big oof").queue());
						})
				)
				.setEphemeral(true)
				.queue();
	}

	@JDAButtonListener(name = HANDLER_NAME)
	public void leBouton(ButtonEvent event, @AppOption long xd, JDA jda) {
		event.reply("Le bouton c le bouton " + xd + " : " + jda)
				.setEphemeral(true)
				.queue();
	}
}

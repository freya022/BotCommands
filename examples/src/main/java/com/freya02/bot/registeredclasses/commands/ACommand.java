package com.freya02.bot.registeredclasses.commands;

import com.freya02.bot.registeredclasses.SomeObject;
import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.JDA;

@CommandMarker //Just so the class isn't marked as unused
public class ACommand extends ApplicationCommand {
	private final SomeObject object;

	@Dependency
	private JDA jda;

	public ACommand(SomeObject object) {
		this.object = object;
	}

	@JDASlashCommand(name = "test")
	public void run(GuildSlashEvent event) {
		System.out.println("jda = " + jda);
		System.out.println("object = " + object);

		event.reply("Done !")
				.setEphemeral(true)
				.queue();
	}
}

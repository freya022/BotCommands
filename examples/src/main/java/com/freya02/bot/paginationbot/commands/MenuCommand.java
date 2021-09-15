package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.pagination.Paginator;
import com.freya02.botcommands.api.pagination.menu.MenuBuilder;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class MenuCommand extends ApplicationCommand {
	@JdaSlashCommand(name = "menu")
	public void run(GuildSlashEvent event) {
		final List<Guild> entries = new ArrayList<>(event.getJDA().getGuilds());

		//Only the caller can use the menu
		// There must be no delete button as the message is ephemeral
		final Paginator paginator = new MenuBuilder<>(event.getUser().getIdLong(), false, entries)
				//Transforms each entry (a Guild) into this text
				.setTransformer(guild -> String.format("%s (%s)", guild.getName(), guild.getId()))
				//Show only 1 entry per page
				.setMaxEntriesPerPage(1)
				//Set the entry (row) prefix
				// This will then display as
				// - GuildName (GuildId)
				.setRowPrefix((entry, maxEntry) -> " - ")
				.build();

		//You must send the menu as a message
		event.reply(paginator.get())
				.setEphemeral(true)
				.queue();
	}
}
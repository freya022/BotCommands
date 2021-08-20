package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.annotation.CommandMarker;
import com.freya02.botcommands.pagination.Paginator;
import com.freya02.botcommands.pagination.menu.ChoiceMenuBuilder;
import com.freya02.botcommands.application.GuildSlashEvent;
import com.freya02.botcommands.application.SlashCommand;
import com.freya02.botcommands.application.slash.annotations.JdaSlashCommand;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class ChoiceMenuCommand extends SlashCommand {
	@JdaSlashCommand(name = "choicemenu")
	public void run(GuildSlashEvent event) {
		final List<Guild> entries = new ArrayList<>(event.getJDA().getGuilds());

		//Only the caller can use the choice menu
		// There must be no delete button as the message is ephemeral
		// The choice menu is like a menu except the user has buttons to choose an entry, 
		//      and you can wait or use a callback to use the user choice
		final Paginator paginator = new ChoiceMenuBuilder<>(event.getUser().getIdLong(), false, entries)
				//Transforms each entry (a Guild) into this text
				.setTransformer(guild -> String.format("%s (%s)", guild.getName(), guild.getId()))
				//Show only 1 entry per page
				.setMaxEntriesPerPage(1)
				//Set the entry (row) prefix
				// This will then display as
				// - GuildName (GuildId)
				.setRowPrefix((entry, maxEntry) -> " - ")
				//This gets called when the user chooses an entry via the buttons
				// First callback parameter is the button event and the second is the choosed entry (the Guild)
				.setCallback((btnEvt, guild) -> {
					//Edit the message with a Message so everything is replaced, instead of just the content
					btnEvt.editMessage(new MessageBuilder("You chose the guild '" + guild.getName() + "' !").build()).queue();
				})
				.build();

		//You must send the menu as a message
		event.reply(paginator.get())
				.setEphemeral(true)
				.queue();
	}
}
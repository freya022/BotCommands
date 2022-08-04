package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenu;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenuBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class ChoiceMenuCommand extends ApplicationCommand {
	@JDASlashCommand(name = "choicemenu")
	public void run(GuildSlashEvent event) {
		final List<Guild> entries = new ArrayList<>(event.getJDA().getGuilds());

		// The choice menu is like a menu except the user has buttons to choose an entry, 
		//      and you can wait or use a callback to use the user choice
		final ChoiceMenu<Guild> paginator = new ChoiceMenuBuilder<>(entries)
				//Only the caller can use the choice menu
				.setConstraints(InteractionConstraints.ofUsers(event.getUser()))
				// There must be no delete button as the message is ephemeral
				.useDeleteButton(false)
				//Transforms each entry (a Guild) into this text
				.setTransformer(guild -> String.format("%s (%s)", guild.getName(), guild.getId()))
				//Show only 1 entry per page
				.setMaxEntriesPerPage(1)
				//Set the entry (row) prefix
				// This will then display as
				// - GuildName (GuildId)
				.setRowPrefixSupplier((entry, maxEntry) -> " - ")
				//This gets called when the user chooses an entry via the buttons
				// First callback parameter is the button event and the second is the chosen entry (the Guild)
				.setCallback((btnEvt, guild) -> {
					//Edit the message with a Message so everything is replaced, instead of just the content
					btnEvt.editMessage(new MessageBuilder("You chose the guild '" + guild.getName() + "' !").build()).queue();
				})
				//This determines what the buttons look like
				.setButtonContentSupplier((item, index) -> ButtonContent.withString(String.valueOf(index + 1)))
				.build();

		//You must send the menu as a message
		event.reply(paginator.get())
				.setEphemeral(true)
				.queue();
	}
}
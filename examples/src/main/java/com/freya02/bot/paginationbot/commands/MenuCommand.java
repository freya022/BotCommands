package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.menu.Menu;
import com.freya02.botcommands.api.pagination.menu.MenuBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class MenuCommand extends ApplicationCommand {
	@JDASlashCommand(name = "menu")
	public void run(GuildSlashEvent event) {
		final List<Guild> entries = new ArrayList<>(event.getJDA().getGuilds());

		//Only the caller can use the menu
		// There must be no delete button as the message is ephemeral
		final Menu<Guild> paginator = new MenuBuilder<>(entries)
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
				.build();

		//You must send the menu as a message
		event.reply(MessageCreateData.fromEditData(paginator.get()))
				.setEphemeral(true)
				.queue();
	}
}
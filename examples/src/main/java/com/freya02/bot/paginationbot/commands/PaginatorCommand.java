package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.paginator.PaginatorBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class PaginatorCommand extends ApplicationCommand {
	@JDASlashCommand(name = "paginator")
	public void run(GuildSlashEvent event) {
		final List<EmbedBuilder> embedBuilders = new ArrayList<>();

		//Let's suppose you generated embeds like in JDA-U, so you'd have a collection of embeds to present
		for (int i = 0; i < 5; i++) {
			embedBuilders.add(new EmbedBuilder().setTitle("Page #" + (i + 1)));
		}

		final Paginator paginator = new PaginatorBuilder()
				//Only the caller can use the choice menu
				.setConstraints(InteractionConstraints.ofUsers(event.getUser()))
				// There must be no delete button as the message is ephemeral
				.useDeleteButton(false)
				// There is 5 pages for the paginator
				.setMaxPages(5)
				.setPaginatorSupplier((instance, messageBuilder, components, page) -> embedBuilders.get(page).build())
				.build();

		//You must send the paginator as a message
		event.reply(MessageCreateData.fromEditData(paginator.get()))
				.setEphemeral(true)
				.queue();
	}
}
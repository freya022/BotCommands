package com.freya02.bot.paginationbot.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.pagination.Paginator;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

@CommandMarker
public class PaginatorCommand extends ApplicationCommand {
	@JDASlashCommand(name = "paginator")
	public void run(GuildSlashEvent event) {
		final List<EmbedBuilder> embedBuilders = new ArrayList<>();

		//Lets suppose you generated embeds like in JDA-U, so you'd have a collection of embeds to present
		for (int i = 0; i < 5; i++) {
			embedBuilders.add(new EmbedBuilder().setTitle("Page #" + (i + 1)));
		}
		
		//Only the caller can use the paginator
		// There is 5 pages for the paginator
		// There must be no delete button as the message is ephemeral
		final Paginator paginator = new Paginator(event.getUser().getIdLong(), 5, false, (builder, components, page) -> {
			//Page is from 0 included to 4 included (5 pages)
			final EmbedBuilder embedBuilder = embedBuilders.get(page);

			//set the builder to be embedBuilder
			builder.copyFrom(embedBuilder);
		});

		//You must send the paginator as a message
		event.reply(paginator.get())
				.setEphemeral(true)
				.queue();
	}
}
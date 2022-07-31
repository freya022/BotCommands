package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.paginator.PaginatorBuilder;
import com.freya02.botcommands.api.utils.ButtonContent;
import com.freya02.botcommands.api.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.concurrent.TimeUnit;

public class SlashPaginator extends ApplicationCommand {
	@JDASlashCommand(name = "paginator", subcommand = "user")
	public void paginatorUser(GuildSlashEvent event, BContext context) {
		replyPaginator(event, context, InteractionConstraints.ofUsers(event.getUser()));
	}

	@JDASlashCommand(name = "paginator", subcommand = "role")
	public void paginatorRole(GuildSlashEvent event, BContext context, @AppOption Role role) {
		replyPaginator(event, context, InteractionConstraints.ofRoles(role));
	}

	@JDASlashCommand(name = "paginator", subcommand = "permissions")
	public void paginatorPermissions(GuildSlashEvent event, BContext context) {
		replyPaginator(event, context, InteractionConstraints.ofPermissions(Permission.MANAGE_THREADS));
	}

	private void replyPaginator(GuildSlashEvent event, BContext context, InteractionConstraints constraints) {
		final PaginatorBuilder builder = new PaginatorBuilder()
				.setConstraints(constraints)
				.useDeleteButton(true)
				.setTimeout(5, TimeUnit.SECONDS, (paginator, message) -> {
					paginator.cleanup(context);

					//Remove components on timeout
					event.getHook().editOriginalComponents().queue();

					//Disable all components on timeout, more expensive
//					event.getHook()
//							.retrieveOriginal()
//							.flatMap(m -> m.editMessageComponents(m.getActionRows().stream().map(ActionRow::asDisabled).toList()))
//							.queue();
				})
				.setMaxPages(5)
				.setFirstContent(ButtonContent.withString("←"))
				.setPaginatorSupplier((paginator, messageBuilder, components, page) -> {
					components.addComponents(1, Components.primaryButton(btnEvt -> {
						paginator.setPage(2); //Pages starts at 0

						btnEvt.editMessage(paginator.get()).queue();
					}).build(ButtonContent.withEmoji("Go to page 3", EmojiUtils.resolveJDAEmoji("page_facing_up"))));

					components.addComponents(1, Components.primaryButton(btnEvt -> {
						paginator.setPage(4); //Pages starts at 0

						btnEvt.editMessage(paginator.get()).queue();
					}).build(ButtonContent.withEmoji("Go to page 5", EmojiUtils.resolveJDAEmoji("page_facing_up"))));

					return new EmbedBuilder()
							.setTitle("Page #" + (page + 1)) //Pages starts at 0
							.build();
				});

		final Paginator paginator = builder.build();

		event.reply(MessageCreateData.fromEditData(paginator.get()))
				.setEphemeral(false)
				.queue();
	}
}

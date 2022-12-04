package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.new_components.Components;
import com.freya02.botcommands.api.new_components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.paginator.PaginatorBuilder;
import com.freya02.botcommands.api.utils.ButtonContent;
import com.freya02.botcommands.api.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.concurrent.TimeUnit;

public class SlashPaginator extends ApplicationCommand {
	@JDASlashCommand(name = "paginator", subcommand = "user")
	public void paginatorUser(GuildSlashEvent event, Components components) {
		replyPaginator(event, InteractionConstraints.ofUsers(event.getUser()), components);
	}

	@JDASlashCommand(name = "paginator", subcommand = "role")
	public void paginatorRole(GuildSlashEvent event, @AppOption Role role, Components components) {
		replyPaginator(event, InteractionConstraints.ofRoles(role), components);
	}

	@JDASlashCommand(name = "paginator", subcommand = "permissions")
	public void paginatorPermissions(GuildSlashEvent event, Components components) {
		replyPaginator(event, InteractionConstraints.ofPermissions(Permission.MANAGE_THREADS), components);
	}

	private void replyPaginator(GuildSlashEvent event, InteractionConstraints constraints, Components componentss) {
		final PaginatorBuilder builder = new PaginatorBuilder()
				.setConstraints(constraints)
				.useDeleteButton(true)
				.setTimeout(5, TimeUnit.SECONDS, (paginator, message) -> {
					paginator.cleanup();

					//Remove components on timeout
					event.getHook().editOriginalComponents().queue();

					//Disable all components on timeout, more expensive
//					event.getHook()
//							.retrieveOriginal()
//							.flatMap(m -> event.getHook().editOriginalComponents(m.getActionRows().stream().map(ActionRow::asDisabled).toList()))
//							.queue();
				})
				.setMaxPages(5)
				.setFirstContent(ButtonContent.withString("←"))
				.setPaginatorSupplier((paginator, messageBuilder, components, page) -> {
					components.addComponents(
							componentss.ephemeralButton(ButtonStyle.PRIMARY, ButtonContent.withEmoji("Go to page 3", EmojiUtils.resolveJDAEmoji("page_facing_up")), buttonBuilder -> {
								buttonBuilder.bindTo(btnEvt -> {
									paginator.setPage(2); //Pages starts at 0

									btnEvt.editMessage(paginator.get()).queue();
								});
							}),

							componentss.ephemeralButton(ButtonStyle.PRIMARY, ButtonContent.withEmoji("Go to page 5", EmojiUtils.resolveJDAEmoji("page_facing_up")), buttonBuilder -> {
								buttonBuilder.bindTo(btnEvt -> {
									paginator.setPage(4); //Pages starts at 0

									btnEvt.editMessage(paginator.get()).queue();
								});
							})
					);

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

package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.paginator.Paginator;
import com.freya02.botcommands.api.pagination.paginator.PaginatorBuilder;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

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
				.setTimeout(5, TimeUnit.SECONDS, (paginator, message) -> {
					paginator.cleanup(context);

					System.out.println("oof");
				})
				.setMaxPages(5)
				.setFirstContent(ButtonContent.withString("←"))
				.setPaginatorSupplier((messageBuilder, components, page) -> {
					return new EmbedBuilder().setTitle("Page #" + page).build();
				});

		final Paginator paginator = builder.build();

		event.reply(paginator.get())
				.setEphemeral(false)
				.queue();
	}
}

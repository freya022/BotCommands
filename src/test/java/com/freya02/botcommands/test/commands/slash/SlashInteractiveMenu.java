package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.data.InteractionConstraints;
import com.freya02.botcommands.api.pagination.interactive.InteractiveMenu;
import com.freya02.botcommands.api.pagination.interactive.InteractiveMenuBuilder;
import com.freya02.botcommands.api.pagination.interactive.SelectContent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.concurrent.TimeUnit;

public class SlashInteractiveMenu extends ApplicationCommand {
	@JDASlashCommand(name = "interactive")
	public void interactiveMenu(GuildSlashEvent event, Components componentss) {
		final InteractiveMenu menu = new InteractiveMenuBuilder()
				.usePaginator(true)
				.addMenu(SelectContent.of("Joy", "This sparks joy", Emoji.fromUnicode("\uD83D\uDE02")), 3, (interactiveMenu, pageNumber, editBuilder, components) -> {
					editBuilder.setContent(":joy:");
					editBuilder.setReplace(true);

					components.addComponents(
							componentss.ephemeralButton(ButtonStyle.DANGER, "Delete", builder -> {
								builder.bindTo(buttonEvent -> {
									event.getHook().deleteOriginal().queue();

									interactiveMenu.cancelTimeout();

									interactiveMenu.cleanup();
								});
							}),

							componentss.ephemeralButton(ButtonStyle.SECONDARY, "Go to 'Grin'", builder -> {
								builder.bindTo(buttonEvent -> {
									interactiveMenu.setSelectedItem("Grin");

									buttonEvent.editMessage(interactiveMenu.get()).queue();
								});
							}));

					return new EmbedBuilder().setTitle("This sparks joy").setDescription("Page #" + pageNumber).build();
				})
				.addMenu(SelectContent.of("Grin", "This does not spark joy", Emoji.fromUnicode("\uD83D\uDE00")), 3, (interactiveMenu, pageNumber, editBuilder, components) -> {
//					editBuilder.setReplace(true);

					components.addComponents(
							componentss.ephemeralButton(ButtonStyle.DANGER, "Delete", builder -> {
								builder.bindTo(buttonEvent -> {
									event.getHook().deleteOriginal().queue();

									interactiveMenu.cancelTimeout();

									interactiveMenu.cleanup();
								});
							}),

							componentss.ephemeralButton(ButtonStyle.SECONDARY, "Go to 'Joy'", builder -> {
								builder.bindTo(buttonEvent -> {
									interactiveMenu.setSelectedItem(0);

									buttonEvent.editMessage(interactiveMenu.get()).queue();
								});
							}));

					return new EmbedBuilder().setTitle("This does not spark joy").setDescription("Page #" + pageNumber).build();
				})
				.setConstraints(InteractionConstraints.ofUsers(event.getUser()))
				.setTimeout(5, TimeUnit.SECONDS, (interactiveMenu, msg) -> {
					System.out.println("bru");

					interactiveMenu.cleanup();
				})
				.build();

		event.reply(MessageCreateData.fromEditData(menu.get())).setEphemeral(false).queue();
	}
}

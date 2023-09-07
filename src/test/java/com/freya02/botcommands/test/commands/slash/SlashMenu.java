package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.data.InteractionConstraints;
import com.freya02.botcommands.api.core.service.annotations.Dependencies;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenu;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenuBuilder;
import com.freya02.botcommands.api.pagination.menu.Menu;
import com.freya02.botcommands.api.pagination.menu.MenuBuilder;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.stream.IntStream;

@Dependencies(Components.class)
public class SlashMenu extends ApplicationCommand {
	@JDASlashCommand(name = "choicemenu")
	public void choicemenu(GuildSlashEvent event, Components components) {
		final ChoiceMenu<Integer> menu = new ChoiceMenuBuilder<>(components, IntStream.rangeClosed(1, 10).boxed().toList())
				.useDeleteButton(false)
				.setConstraints(InteractionConstraints.ofUsers(event.getUser()))
				.setFirstContent(ButtonContent.withString("←"))
				.setCallback((event1, textChannel) -> {
					event1.deferEdit().queue();

					System.out.println("Choose " + textChannel);
				})
				.setButtonContentSupplier((item, index) -> ButtonContent.withString(String.valueOf(index + 1)))
				.setMaxEntriesPerPage(3)
				.build();

		event.reply(MessageCreateData.fromEditData(menu.get()))
				.setEphemeral(false)
				.queue();
	}

	@JDASlashCommand(name = "normalmenu")
	public void menu(GuildSlashEvent event, Components components) {
		final Menu<Integer> menu = new MenuBuilder<>(components, IntStream.rangeClosed(1, 10).boxed().toList())
				.useDeleteButton(false)
				.setConstraints(InteractionConstraints.ofUsers(event.getUser()))
				.setFirstContent(ButtonContent.withString("←"))
				.setMaxEntriesPerPage(3)
				.build();

		event.reply(MessageCreateData.fromEditData(menu.get()))
				.setEphemeral(false)
				.queue();
	}
}

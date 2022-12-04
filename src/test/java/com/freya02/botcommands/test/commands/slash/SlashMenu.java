package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.new_components.InteractionConstraints;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenu;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenuBuilder;
import com.freya02.botcommands.api.pagination.menu.Menu;
import com.freya02.botcommands.api.pagination.menu.MenuBuilder;
import com.freya02.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.stream.IntStream;

public class SlashMenu extends ApplicationCommand {
	@JDASlashCommand(name = "choicemenu")
	public void choicemenu(GuildSlashEvent event) {
		final ChoiceMenu<Integer> menu = new ChoiceMenuBuilder<>(IntStream.rangeClosed(1, 10).boxed().toList())
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
	public void menu(GuildSlashEvent event) {
		final Menu<Integer> menu = new MenuBuilder<>(IntStream.rangeClosed(1, 10).boxed().toList())
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

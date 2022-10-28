package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.builder.selects.LambdaEntitySelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.selects.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.selects.LambdaStringSelectionMenuBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SlashSelectMenu extends ApplicationCommand {
	@NotNull
	@Override
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		if (optionIndex == 0) {
			return List.of(
					new Command.Choice("String", "String"),
					new Command.Choice("Role", "Role"),
					new Command.Choice("User", "User"),
					new Command.Choice("Channel", "Channel"),
					new Command.Choice("Channel (Category)", "Channel (Category)")
			);
		}

		return super.getOptionChoices(guild, commandPath, optionIndex);
	}

	private <T extends SelectMenu.Builder<R, ?> & LambdaSelectionMenuBuilder<?, ?>, R extends SelectMenu> R finishMenu(T builder) {
		builder.timeout(10, TimeUnit.SECONDS, () -> System.out.println("When the select menu is dead"));
		builder.addPermissions(Permission.ADMINISTRATOR, Permission.MANAGE_CHANNEL);
		builder.oneUse();
		return builder
				.setPlaceholder("Select something ?")
				.setMaxValues(3)
				.setMinValues(2)
				.build();
	}

	@JDASlashCommand(name = "select_menu")
	public void onSlashSelectMenu(GuildSlashEvent event, @AppOption String selectType) {
		final SelectMenu menu = switch (selectType) {
			case "String" -> finishMenu((LambdaStringSelectionMenuBuilder) Components.stringSelectionMenu(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue()).addOption("Test", "Test"));
			case "Role" -> finishMenu((LambdaEntitySelectionMenuBuilder) Components.entitySelectionMenu(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue()).setEntityTypes(EntitySelectMenu.SelectTarget.ROLE));
			case "User" -> finishMenu((LambdaEntitySelectionMenuBuilder) Components.entitySelectionMenu(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue()).setEntityTypes(EntitySelectMenu.SelectTarget.USER));
			case "Channel" -> finishMenu((LambdaEntitySelectionMenuBuilder) Components.entitySelectionMenu(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue()).setEntityTypes(EntitySelectMenu.SelectTarget.CHANNEL));
			case "Channel (Category)" -> finishMenu((LambdaEntitySelectionMenuBuilder) Components.entitySelectionMenu(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue()).setEntityTypes(EntitySelectMenu.SelectTarget.CHANNEL).setChannelTypes(ChannelType.CATEGORY));
			default -> throw new IllegalArgumentException("Unknown select type: " + selectType);
		};

		event.replyComponents(ActionRow.of(menu))
				.setEphemeral(true)
				.queue();
	}
}

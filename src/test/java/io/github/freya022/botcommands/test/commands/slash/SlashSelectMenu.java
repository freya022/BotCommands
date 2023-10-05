package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.builder.BaseComponentBuilder;
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent;
import io.github.freya022.botcommands.api.components.builder.IUniqueComponent;
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Dependencies(Components.class)
public class SlashSelectMenu extends ApplicationCommand {
	@NotNull
	@Override
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String optionName) {
		if (optionName.equals("select_type")) {
			return List.of(
					new Command.Choice("String", "String"),
					new Command.Choice("Role", "Role"),
					new Command.Choice("User", "User"),
					new Command.Choice("Channel", "Channel"),
					new Command.Choice("Channel (Category)", "Channel (Category)")
			);
		}

		return super.getOptionChoices(guild, commandPath, optionName);
	}

	private <T extends SelectMenu.Builder<?, ?> & IEphemeralTimeoutableComponent & BaseComponentBuilder & IUniqueComponent> void finishMenu(T builder) {
		builder.timeout(10, TimeUnit.SECONDS, () -> System.out.println("When the select menu is dead"));
		builder.constraints(c -> c.addPermissions(Permission.ADMINISTRATOR, Permission.MANAGE_CHANNEL));
		builder.setOneUse(true);
		builder
				.setPlaceholder("Select something ?")
				.setMaxValues(3)
				.setMinValues(2)
				.build();
	}

	@JDASlashCommand(name = "select_menu")
	public void onSlashSelectMenu(GuildSlashEvent event, @SlashOption String selectType, Components components) {
		final SelectMenu menu = switch (selectType) {
			case "String" -> components.ephemeralStringSelectMenu(builder -> {
				builder.bindTo(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue());
				builder.addOption("Test", "Test");
				finishMenu(builder);
			});
			case "Role" -> components.ephemeralEntitySelectMenu(SelectTarget.ROLE, builder -> {
				builder.bindTo(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue());
				finishMenu(builder);
			});
			case "User" -> components.ephemeralEntitySelectMenu(SelectTarget.USER, builder -> {
				builder.bindTo(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue());
				finishMenu(builder);
			});
			case "Channel" -> components.ephemeralEntitySelectMenu(SelectTarget.CHANNEL, builder -> {
				builder.bindTo(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue());
				finishMenu(builder);
			});
			case "Channel (Category)" -> components.ephemeralEntitySelectMenu(SelectTarget.ROLE, builder -> {
				builder.bindTo(selectEvt -> selectEvt.reply("Values: " + selectEvt.getValues()).queue());
				builder.setChannelTypes(ChannelType.CATEGORY);
				finishMenu(builder);
			});
			default -> throw new IllegalArgumentException("Unknown select type: " + selectType);
		};

		event.replyComponents(ActionRow.of(menu))
				.setEphemeral(true)
				.queue();
	}
}

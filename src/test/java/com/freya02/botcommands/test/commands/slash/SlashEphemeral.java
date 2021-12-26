package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.ChoiceList;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlashEphemeral extends ApplicationCommand {
	@NotNull
	@Override
	public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		if (optionIndex == 0) {
			return ChoiceList.ofSize(3);
		} else {
			return List.of();
		}
	}

	@JDASlashCommand(name = "a", group = "b", subcommand = "c")
	public void runEphemeral(GuildSlashEvent event,
	                         @AppOption String message,
	                         @AppOption User user,
	                         @AppOption Role role,
	                         @AppOption TextChannel textChannel,
	                         BContext context,
	                         JDA jda) {
		event.reply("test")
				.setEphemeral(true)
				.flatMap(InteractionHook::retrieveOriginal)
				.queue(m -> {
					System.out.println(m.getJumpUrl());
				});
	}
}

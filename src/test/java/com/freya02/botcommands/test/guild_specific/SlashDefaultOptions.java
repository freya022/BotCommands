package com.freya02.botcommands.test.guild_specific;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.DefaultValue;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.Default;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlashDefaultOptions extends ApplicationCommand {
	@Override
	@Nullable
	public Boolean isDefaultValueEnabled(@NotNull BContext context, @NotNull Guild guild,
	                                     @Nullable String commandId, @NotNull CommandPath commandPath,
	                                     @NotNull String optionName, @NotNull Class<?> parameterType) {
		if (guild.getIdLong() != 722891685755093072L) { //Push default values only outside the test guild
			if (commandPath.toString().equals("default")) {
				if (optionName.equals("user")) {
					return true;
				}
			}
		}

		return super.isDefaultValueEnabled(context, guild, commandId, commandPath, optionName, parameterType);
	}

	@Override
	@Nullable
	public DefaultValue getDefaultValue(@NotNull BContext context, @NotNull Guild guild,
	                                    @Nullable String commandId, @NotNull CommandPath commandPath,
	                                    @NotNull String optionName, @NotNull Class<?> parameterType) {
		if (guild.getIdLong() != 722891685755093072L) { //Push default values only outside the test guild
			if (commandPath.toString().equals("default")) {
				if (optionName.equals("user")) {
					return new DefaultValue(context.getJDA().retrieveUserById(222046562543468545L).complete());
				}
			}
		}

		return super.getDefaultValue(context, guild, commandId, commandPath, optionName, parameterType);
	}

	@JDASlashCommand(name = "default")
	public void run(GuildSlashEvent event, @Default @AppOption User user) {
		event.reply("user " + user.getAsMention() + " ok")
				.setEphemeral(true)
				.queue();
	}
}

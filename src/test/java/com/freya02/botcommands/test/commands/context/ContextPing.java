package com.freya02.botcommands.test.commands.context;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class ContextPing extends ApplicationCommand {
//	@RequireOwner
	@JDAUserCommand(name = "Get pinged")
	public void ping(GuildUserEvent event, @AppOption User target) {
		target.openPrivateChannel().queue(p -> p.sendMessage("Get pinged, courtesy of " + event.getUser().getAsMention())
				.queue(msg -> {
					event.reply("This dood got pinged").setEphemeral(true).queue();
				}, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
					event.reply("Can't send him a DM").setEphemeral(true).queue();
				}))
		);
	}
}

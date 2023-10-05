package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.annotations.CommandMarker;
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions;
import io.github.freya022.botcommands.api.commands.annotations.Optional;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.Category;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.Description;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@CommandMarker //No unused warnings
@Category("Moderation")
@Description("Kicks someone")
@BotPermissions(Permission.KICK_MEMBERS) //The bot need those permissions for others to use the command
@UserPermissions(Permission.KICK_MEMBERS) //The user need those permissions to use this command
public class KickTest extends TextCommand {
	//This method gets executed if the command looks either like
	// !kick @freya02
	// or
	// !kick @freya02 get nae nae'd
	@JDATextCommand(name = "kick")
	public void exec(BaseCommandEvent event,
                     @TextOption Member member,
                     @TextOption(example = "Spam") /* In the help content this parameter will have 'Spam' as a reason example */ @Optional String reason) {
		event.getGuild().kick(member).reason(reason).queue();
	}
}
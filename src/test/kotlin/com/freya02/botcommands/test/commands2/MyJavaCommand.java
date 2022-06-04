package com.freya02.botcommands.test.commands2;

import com.freya02.botcommands.annotations.api.annotations.CommandMarker;
import com.freya02.botcommands.annotations.api.annotations.Optional;
import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.Name;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import net.dv8tion.jda.api.entities.User;

public class MyJavaCommand extends ApplicationCommand {
	@CommandMarker
	public void cmd(GuildSlashEvent event,
	                       String stringOption,
	                       @Name(name = "int", declaredName = "notIntOption") int intOption,
	                       @Name(name = "user") User userOption,
	                       @Name(declaredName = "notDoubleOption") @Optional double doubleOption,
	                       BContext custom) {
		event.reply(stringOption + intOption + doubleOption + userOption + custom).queue();
	}
}

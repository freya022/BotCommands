package com.freya02.botcommands.regex;

import com.freya02.botcommands.exceptions.BadIdException;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface PatternSolver {
	Object solve(GuildMessageReceivedEvent event, String[] groups) throws BadIdException;
}

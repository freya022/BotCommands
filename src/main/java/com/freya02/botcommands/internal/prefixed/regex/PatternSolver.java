package com.freya02.botcommands.internal.prefixed.regex;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface PatternSolver {
	Object solve(GuildMessageReceivedEvent event, String[] groups);
}

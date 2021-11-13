package com.freya02.botcommands.api.application;

import net.dv8tion.jda.api.entities.Guild;

public record CommandUpdateResult(Guild guild, boolean updatedCommands, boolean updatedPrivileges) {}

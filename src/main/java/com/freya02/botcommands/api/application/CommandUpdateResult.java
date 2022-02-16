package com.freya02.botcommands.api.application;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Record which holds the result of a scheduled command update
 * <br>This tells you if the commands were updated in the specified guild, and/or if the privileges were updated
 */
public record CommandUpdateResult(Guild guild, boolean updatedCommands, boolean updatedPrivileges) {}

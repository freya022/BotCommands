package com.freya02.botcommands.api.commands.application;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Record which holds the result of a scheduled command update
 * <br>This tells you if the commands were updated in the specified scope
 */
public record CommandUpdateResult(@Nullable Guild guild, boolean updatedCommands, List<CommandUpdateException> updateExceptions) {}

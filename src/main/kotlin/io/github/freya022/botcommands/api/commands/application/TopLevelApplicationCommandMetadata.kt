package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import java.time.OffsetDateTime

/**
 * Discord's metadata about an application command.
 */
interface TopLevelApplicationCommandMetadata {
    /**
     * The version of this application command,
     * this is a snowflake that contains the time at which the command was created/updated.
     *
     * @see timeModified
     */
    val version: Long

    /**
     * The ID of this application command,
     * this is a snowflake that contains the time at which the command was created.
     *
     * Does not change after an update, can be used to [make a mention][TopLevelSlashCommandInfo.asMention].
     */
    val id: Long

    /**
     * The time at which this application command was created/modified, retrieved from the [version].
     */
    val timeModified: OffsetDateTime

    /**
     * The ID of the guild on which this command was pushed on.
     */
    val guildId: Long?
}
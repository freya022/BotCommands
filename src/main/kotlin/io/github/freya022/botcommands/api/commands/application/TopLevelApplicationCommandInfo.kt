package io.github.freya022.botcommands.api.commands.application

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel
import java.time.OffsetDateTime

/**
 * Represents a top-level application command (i.e. not a subcommand, nor a group).
 *
 * Contains additional info only available on top-level commands.
 */
interface TopLevelApplicationCommandInfo : ApplicationCommandInfo, TopLevelApplicationCommandMetadata, ISnowflake {
    /**
     * The scope on which this application command is pushed on.
     */
    val scope: CommandScope

    /**
     * Whether this application command is (initially) locked to administrators.
     *
     * Administrators can then set up who can use the application command.
     */
    val isDefaultLocked: Boolean

    /**
     * Whether this application command is usable only in guilds (i.e., no DMs).
     */
    val isGuildOnly: Boolean

    /**
     * Whether this application commands is usable only in [NSFW channels][IAgeRestrictedChannel].
     */
    val nsfw: Boolean

    /**
     * Discord's metadata about this application command.
     */
    val metadata: TopLevelApplicationCommandMetadata

    override fun getIdLong(): Long = metadata.id

    override val version: Long
        get() = metadata.version
    override val id: Long
        get() = metadata.id
    override val timeModified: OffsetDateTime
        get() = metadata.timeModified
}
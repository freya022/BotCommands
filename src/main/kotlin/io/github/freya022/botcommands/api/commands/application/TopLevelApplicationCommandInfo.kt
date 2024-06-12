package io.github.freya022.botcommands.api.commands.application

import net.dv8tion.jda.api.entities.ISnowflake
import java.time.OffsetDateTime

interface TopLevelApplicationCommandInfo : TopLevelApplicationCommandMetadata, ISnowflake {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
    val nsfw: Boolean

    val metadata: TopLevelApplicationCommandMetadata

    override fun getIdLong(): Long = metadata.id

    override val version: Long
        get() = metadata.version
    override val id: Long
        get() = metadata.id
    override val timeModified: OffsetDateTime
        get() = metadata.timeModified
}
package io.github.freya022.botcommands.api.commands.application

import java.time.OffsetDateTime

interface TopLevelApplicationCommandMetadata {
    val version: Long
    val id: Long
    val timeModified: OffsetDateTime
}
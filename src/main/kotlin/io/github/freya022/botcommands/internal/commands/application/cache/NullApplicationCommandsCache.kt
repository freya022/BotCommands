package io.github.freya022.botcommands.internal.commands.application.cache

import io.github.freya022.botcommands.internal.utils.throwInternal

internal data object NullApplicationCommandsCache : ApplicationCommandsCache {
    override fun hasCommands(): Boolean = false

    override fun hasMetadata(): Boolean = false

    override fun readCommands(): String = throwInternal("Cannot read commands")

    override fun readMetadata(): String = throwInternal("Cannot read metadata")

    override fun writeCommands(bytes: ByteArray) {}

    override fun writeMetadata(bytes: ByteArray) {}
}
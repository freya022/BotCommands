package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KLogger
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command

internal interface SkipLogger {
    fun skip(name: String, reason: String)

    fun skip(path: CommandPath, reason: String)

    fun log(guild: Guild?, type: Command.Type)
}

internal fun SkipLogger(logger: KLogger): SkipLogger = when {
    logger.isDebugEnabled() -> SkipLoggerImpl(logger)
    else -> NoopSkipLogger
}

internal class SkipLoggerImpl(private val logger: KLogger) : SkipLogger {
    private class SkippedCommand(val path: CommandPath, val reason: String)

    private val skips: MutableList<SkippedCommand> = arrayListOf()

    override fun skip(name: String, reason: String) {
        skips += SkippedCommand(CommandPath.ofName(name), reason)
    }

    override fun skip(path: CommandPath, reason: String) {
        skips += SkippedCommand(path, reason)
    }

    override fun log(guild: Guild?, type: Command.Type) {
        if (skips.isEmpty()) return

        if (logger.isTraceEnabled()) {
            logger.trace {
                buildString {
                    appendBase(guild, type)
                    append(":\n")
                    append(skips.joinAsList { "${it.path}: ${it.reason}" })
                }
            }
        } else {
            logger.debug {
                buildString {
                    appendBase(guild, type)
                }
            }
        }
    }

    private fun StringBuilder.appendBase(guild: Guild?, type: Command.Type) {
        append("Skipped ${skips.size}")
        if (guild != null) {
            append(" ${type.toHumanName()} commands in ${guild.name} (${guild.id})")
        } else {
            append(" global ${type.toHumanName()} commands")
        }
    }

    private fun Command.Type.toHumanName() = when (this) {
        Command.Type.UNKNOWN -> throwInternal("Cannot pass UNKNOWN")
        Command.Type.SLASH -> "slash"
        Command.Type.USER -> "user"
        Command.Type.MESSAGE -> "message"
    }
}

internal object NoopSkipLogger : SkipLogger {
    override fun skip(name: String, reason: String) { }
    override fun skip(path: CommandPath, reason: String) { }

    override fun log(guild: Guild?, type: Command.Type) { }
}
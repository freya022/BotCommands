package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.core.utils.enumMapOf
import io.github.freya022.botcommands.internal.commands.application.*
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.interactions.commands.Command
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

abstract class ApplicationCommandMap {
    val allApplicationCommands: @UnmodifiableView List<ApplicationCommandInfo>
        get() = Collections.unmodifiableList(Command.Type.entries.flatMap { getTypeMap<ApplicationCommandInfo>(it).values })

    operator fun get(type: Command.Type, path: CommandPath): ApplicationCommandInfo? {
        return getTypeMap<ApplicationCommandInfo>(type)[path]
    }

    val slashCommands: @UnmodifiableView CommandMap<SlashCommandInfo> get() = getTypeMap(Command.Type.SLASH)
    val userCommands: @UnmodifiableView CommandMap<UserCommandInfo> get() = getTypeMap(Command.Type.USER)
    val messageCommands: @UnmodifiableView CommandMap<MessageCommandInfo> get() = getTypeMap(Command.Type.MESSAGE)

    fun findSlashCommand(path: CommandPath): SlashCommandInfo? = slashCommands[path]
    fun findUserCommand(name: String): UserCommandInfo? = userCommands[CommandPath.ofName(name)]
    fun findMessageCommand(name: String): MessageCommandInfo? = messageCommands[CommandPath.ofName(name)]

    abstract fun <T : ApplicationCommandInfo> getTypeMap(type: Command.Type): CommandMap<T>

    operator fun plus(liveApplicationCommandsMap: ApplicationCommandMap): ApplicationCommandMap {
        val newMap: MutableMap<Command.Type, MutableCommandMap<ApplicationCommandInfo>> = enumMapOf<Command.Type, MutableCommandMap<ApplicationCommandInfo>>()
        Command.Type.entries.forEach { commandType ->
            val commandMap = newMap.getOrPut(commandType) { MutableCommandMap() }

            listOf(this, liveApplicationCommandsMap).forEach { sourceMap ->
                sourceMap.getTypeMap<ApplicationCommandInfo>(commandType).forEach { (path, info) ->
                    commandMap[path] = info
                }
            }
        }

        return MutableApplicationCommandMap(newMap).toUnmodifiableMap()
    }
}

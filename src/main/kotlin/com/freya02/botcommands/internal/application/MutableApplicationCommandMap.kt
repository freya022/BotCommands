package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.ApplicationCommandMap
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import java.util.function.Function
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

class MutableApplicationCommandMap : ApplicationCommandMap() {
    override fun getSlashCommands(): MutableCommandMap<SlashCommandInfo> {
        return getTypeMap(CommandType.SLASH)
    }

    override fun getUserCommands(): MutableCommandMap<UserCommandInfo> {
        return getTypeMap(CommandType.USER)
    }

    override fun getMessageCommands(): MutableCommandMap<MessageCommandInfo> {
        return getTypeMap(CommandType.MESSAGE)
    }

    fun <T : ApplicationCommandInfo> computeIfAbsent(
        type: CommandType,
        path: CommandPath,
        mappingFunction: Function<CommandPath, T>
    ): T = getTypeMap<T>(type).computeIfAbsent(path, mappingFunction)

    fun <T : ApplicationCommandInfo> put(type: CommandType, path: CommandPath, value: T): T? = getTypeMap<T>(type).put(path, value)

    override fun <T : ApplicationCommandInfo> getTypeMap(type: CommandType): MutableCommandMap<T> {
        return super.getTypeMap<T>(type) as MutableCommandMap<T>
    }

    companion object {
        @JvmStatic
        fun fromCommandList(guildApplicationCommands: List<ApplicationCommandInfo>) = MutableApplicationCommandMap().also { map ->
            for (info in guildApplicationCommands) {
                val type = when (info) {
                    is MessageCommandInfo -> CommandType.MESSAGE
                    is UserCommandInfo -> CommandType.USER
                    is SlashCommandInfo -> CommandType.SLASH
                    else -> throw IllegalArgumentException("Unknown application command info type: " + info.javaClass.name)
                }
                map.getTypeMap<ApplicationCommandInfo>(type)[info.path] = info
            }
        }
    }
}
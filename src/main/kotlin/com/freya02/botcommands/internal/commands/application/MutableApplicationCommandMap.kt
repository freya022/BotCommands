package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommandMap
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
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
                    is TopLevelSlashCommandInfo -> CommandType.SLASH
                    else -> throw IllegalArgumentException("Unknown application command info type: " + info.javaClass.name)
                }

                val typeMap = map.getTypeMap<ApplicationCommandInfo>(type)
                when (info) {
                    is UserCommandInfo, is MessageCommandInfo -> typeMap[info.path] = info
                    is TopLevelSlashCommandInfo -> {
                        if (info.isTopLevelCommandOnly()) {
                            typeMap[info.path] = info
                        } else {
                            info.subcommandGroups.values.forEach { groupInfo ->
                                groupInfo.subcommands.values.forEach {
                                    typeMap[it.path] = it
                                }
                            }

                            info.subcommands.values.forEach {
                                typeMap[it.path] = it
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Unknown application command info type: " + info.javaClass.name)
                }
            }
        }
    }
}
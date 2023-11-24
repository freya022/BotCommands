package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandMap
import io.github.freya022.botcommands.api.core.utils.enumMapOf
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*
import java.util.function.Function
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

internal class MutableApplicationCommandMap internal constructor(
    private val rawTypeMap: MutableMap<Command.Type, MutableCommandMap<ApplicationCommandInfo>> = Collections.synchronizedMap(enumMapOf())
) : ApplicationCommandMap() {
    override fun getRawTypeMap() = rawTypeMap

    override fun getSlashCommands(): MutableCommandMap<SlashCommandInfo> {
        return getTypeMap(CommandType.SLASH)
    }

    override fun getUserCommands(): MutableCommandMap<UserCommandInfo> {
        return getTypeMap(CommandType.USER)
    }

    override fun getMessageCommands(): MutableCommandMap<MessageCommandInfo> {
        return getTypeMap(CommandType.MESSAGE)
    }

    internal fun <T : ApplicationCommandInfo> computeIfAbsent(
        type: CommandType,
        path: CommandPath,
        mappingFunction: Function<CommandPath, T>
    ): T = getTypeMap<T>(type).computeIfAbsent(path, mappingFunction)

    internal fun <T : ApplicationCommandInfo> put(type: CommandType, path: CommandPath, value: T): T? = getTypeMap<T>(type).put(path, value)

    override operator fun plus(map: ApplicationCommandMap): MutableApplicationCommandMap {
        val newMap: MutableMap<Command.Type, MutableCommandMap<ApplicationCommandInfo>> = enumMapOf<Command.Type, MutableCommandMap<ApplicationCommandInfo>>()
        Command.Type.entries.forEach { commandType ->
            val commandMap = newMap.getOrPut(commandType) { MutableCommandMap() }

            listOf(this, map).forEach { sourceMap ->
                sourceMap.getTypeMap<ApplicationCommandInfo>(commandType).forEach { (path, info) ->
                    commandMap[path] = info
                }
            }
        }

        return MutableApplicationCommandMap(newMap)
    }

    //TODO make a proper unmodifiable type
    @Suppress("UNCHECKED_CAST")
    override fun <T : ApplicationCommandInfo> getTypeMap(type: CommandType): MutableCommandMap<T> {
        return rawTypeMap.computeIfAbsent(type) { MutableCommandMap() } as MutableCommandMap<T>
    }

    internal companion object {
        @JvmStatic
        val EMPTY_MAP: ApplicationCommandMap = MutableApplicationCommandMap(mutableMapOf())

        internal fun fromCommandList(guildApplicationCommands: Collection<ApplicationCommandInfo>) = MutableApplicationCommandMap().also { map ->
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
package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandMap
import io.github.freya022.botcommands.api.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.utils.enumMapOf
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*
import net.dv8tion.jda.api.interactions.commands.Command.Type as CommandType

internal class MutableApplicationCommandMap internal constructor(
    private val rawTypeMap: MutableMap<Command.Type, MutableCommandMap<ApplicationCommandInfo>> = Collections.synchronizedMap(enumMapOf())
) : ApplicationCommandMap() {
    internal class UnmodifiableApplicationCommandMap(private val map: ApplicationCommandMap) : ApplicationCommandMap() {
        override fun <T : ApplicationCommandInfo> getTypeMap(type: Command.Type): CommandMap<T> =
            map.getTypeMap<T>(type).toUnmodifiableMap()
    }

    @Suppress("UNCHECKED_CAST")
    internal object EmptyApplicationCommandMap : ApplicationCommandMap() {
        override fun <T : ApplicationCommandInfo> getTypeMap(type: Command.Type): CommandMap<T> =
            EmptyCommandMap as CommandMap<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ApplicationCommandInfo> getTypeMap(type: CommandType): MutableCommandMap<T> {
        return rawTypeMap.computeIfAbsent(type) { MutableCommandMap() } as MutableCommandMap<T>
    }

    internal companion object {
        @JvmStatic
        val EMPTY_MAP: ApplicationCommandMap = EmptyApplicationCommandMap

        internal fun fromCommandList(guildApplicationCommands: Collection<ApplicationCommandInfo>) = MutableApplicationCommandMap().also { map ->
            for (info in guildApplicationCommands) {
                val type = when (info) {
                    is MessageCommandInfo -> CommandType.MESSAGE
                    is UserCommandInfo -> CommandType.USER
                    is TopLevelSlashCommandInfo -> CommandType.SLASH
                    else -> throwArgument("Unknown application command info type: " + info.javaClass.name)
                }

                val typeMap = map.getTypeMap<ApplicationCommandInfo>(type)
                when (info) {
                    is UserCommandInfo, is MessageCommandInfo -> typeMap[info.path] = info
                    is TopLevelSlashCommandInfo -> {
                        if (info.isTopLevelCommandOnly) {
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
                    else -> throwArgument("Unknown application command info type: " + info.javaClass.name)
                }
            }
        }
    }
}

internal fun ApplicationCommandMap.toUnmodifiableMap(): ApplicationCommandMap {
    if (this is MutableApplicationCommandMap.UnmodifiableApplicationCommandMap) return this
    return MutableApplicationCommandMap.UnmodifiableApplicationCommandMap(this)
}
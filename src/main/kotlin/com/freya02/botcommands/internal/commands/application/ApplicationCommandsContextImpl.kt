package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommandMap
import com.freya02.botcommands.api.commands.application.ApplicationCommandsContext
import com.freya02.botcommands.api.commands.application.CommandUpdateResult
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.core.BContextImpl
import gnu.trove.TCollections
import gnu.trove.map.hash.TLongObjectHashMap
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture

class ApplicationCommandsContextImpl internal constructor(private val context: BContextImpl) : ApplicationCommandsContext {
    private val liveApplicationCommandInfoMap = TCollections.synchronizedMap(TLongObjectHashMap<ApplicationCommandMap>())

    override fun findLiveSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo? =
        getLiveApplicationCommandsMap(guild).findSlashCommand(path)
            ?: getLiveApplicationCommandsMap(null).findSlashCommand(path)

    override fun findLiveUserCommand(guild: Guild?, name: String): UserCommandInfo? =
        getLiveApplicationCommandsMap(guild).findUserCommand(name)
            ?: getLiveApplicationCommandsMap(null).findUserCommand(name)

    override fun findLiveMessageCommand(guild: Guild?, name: String): MessageCommandInfo? =
        getLiveApplicationCommandsMap(guild).findMessageCommand(name)
            ?: getLiveApplicationCommandsMap(null).findMessageCommand(name)

    override fun getLiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap {
        return liveApplicationCommandInfoMap[getGuildKey(guild)]
    }

    override fun getEffectiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap = when (guild) {
        null -> getLiveApplicationCommandsMap(null)
        else -> getLiveApplicationCommandsMap(null) + getLiveApplicationCommandsMap(guild)
    }

    fun putLiveApplicationCommandsMap(guild: Guild?, map: ApplicationCommandMap) {
        liveApplicationCommandInfoMap.put(getGuildKey(guild), map)
    }

    override fun updateGlobalApplicationCommands(force: Boolean): CompletableFuture<CommandUpdateResult> {
        return CompletableFuture<CommandUpdateResult>().also {
            context.coroutineScopesConfig.commandUpdateScope.launch {
                it.complete(context.getService<ApplicationCommandsBuilder>().updateGlobalCommands(force))
            }
        }
    }

    override fun updateGuildApplicationCommands(guild: Guild, force: Boolean): CompletableFuture<CommandUpdateResult> {
        return CompletableFuture<CommandUpdateResult>().also {
            context.coroutineScopesConfig.commandUpdateScope.launch {
                it.complete(context.getService<ApplicationCommandsBuilder>().updateGuildCommands(guild, force))
            }
        }
    }

    private fun getGuildKey(guild: Guild?): Long {
        return guild?.idLong ?: 0
    }
}
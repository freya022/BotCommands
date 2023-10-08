package io.github.freya022.botcommands.internal.commands.application

import gnu.trove.TCollections
import gnu.trove.map.hash.TLongObjectHashMap
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandMap
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.application.CommandUpdateResult
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.core.BContextImpl
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture

//TODO internal
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
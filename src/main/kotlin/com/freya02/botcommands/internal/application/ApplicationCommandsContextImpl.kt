package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.ApplicationCommandMap
import com.freya02.botcommands.api.application.ApplicationCommandsContext
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandUpdateResult
import com.freya02.botcommands.commands.internal.application.ApplicationCommandsBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import gnu.trove.TCollections
import gnu.trove.map.hash.TLongObjectHashMap
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture

//TODO should be a service ?
class ApplicationCommandsContextImpl(private val context: BContextImpl) : ApplicationCommandsContext {
    val mutableApplicationCommandMap = MutableApplicationCommandMap()

    private val liveApplicationCommandInfoMap = TCollections.synchronizedMap(TLongObjectHashMap<ApplicationCommandMap>())

    override fun findLiveSlashCommand(guild: Guild?, path: CommandPath): SlashCommandInfo? =
        liveApplicationCommandInfoMap[getGuildKey(guild)]?.findSlashCommand(path)

    override fun findLiveUserCommand(guild: Guild?, name: String): UserCommandInfo? =
        liveApplicationCommandInfoMap[getGuildKey(guild)]?.findUserCommand(name)

    override fun findLiveMessageCommand(guild: Guild?, name: String): MessageCommandInfo? =
        liveApplicationCommandInfoMap[getGuildKey(guild)]?.findMessageCommand(name)

    override fun getApplicationCommandMap(): ApplicationCommandMap {
        return mutableApplicationCommandMap
    }

    override fun getLiveApplicationCommandsMap(guild: Guild?): ApplicationCommandMap {
        return liveApplicationCommandInfoMap[getGuildKey(guild)]
    }

    fun putLiveApplicationCommandsMap(guild: Guild?, map: ApplicationCommandMap) {
        liveApplicationCommandInfoMap.put(getGuildKey(guild), map)
    }

    override fun updateGlobalApplicationCommands(force: Boolean): CompletableFuture<CommandUpdateResult> {
        return CompletableFuture<CommandUpdateResult>().also {
            context.config.coroutineScopesConfig.miscScope.launch {
                it.complete(context.getService(ApplicationCommandsBuilder::class).updateGlobalCommands(force))
            }
        }
    }

    override fun updateGuildApplicationCommands(guild: Guild, force: Boolean): CompletableFuture<CommandUpdateResult> {
        return CompletableFuture<CommandUpdateResult>().also {
            context.config.coroutineScopesConfig.miscScope.launch {
                it.complete(context.getService(ApplicationCommandsBuilder::class).updateGuildCommands(guild, force))
            }
        }
    }

    private fun getGuildKey(guild: Guild?): Long {
        return guild?.idLong ?: 0
    }
}
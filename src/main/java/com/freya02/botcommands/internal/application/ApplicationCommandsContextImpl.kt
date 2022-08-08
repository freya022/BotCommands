package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.ApplicationCommandMap
import com.freya02.botcommands.api.application.ApplicationCommandsContext
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import gnu.trove.TCollections
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.entities.Guild
import java.util.*

class ApplicationCommandsContextImpl : ApplicationCommandsContext {
    val mutableApplicationCommandMap = MutableApplicationCommandMap()

    private val liveApplicationCommandInfoMap = TCollections.synchronizedMap(TLongObjectHashMap<ApplicationCommandMap>())
    internal val baseNameToLocalesMap: MutableMap<String, MutableList<Locale>> = hashMapOf()

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

    override fun addLocalizations(bundleName: String, locales: List<Locale>) {
        baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() }.addAll(locales)
    }

    override fun removeLocalizations(bundleName: String, locales: List<Locale>) {
        baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() }.removeAll(locales)
    }

    override fun removeLocalizations(bundleName: String) {
        baseNameToLocalesMap.remove(bundleName)
    }

    private fun getGuildKey(guild: Guild?): Long {
        return guild?.idLong ?: 0
    }
}
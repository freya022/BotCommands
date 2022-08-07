package com.freya02.botcommands.internal

import com.freya02.botcommands.api.CooldownScope
import gnu.trove.TCollections
import gnu.trove.impl.Constants
import gnu.trove.map.hash.TLongLongHashMap
import gnu.trove.map.hash.TObjectLongHashMap
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.math.max

//Trove maps are not *always* the fastest, by a small margin, but are by far the most memory efficient
//The values are the time on which the cooldown expires
private inline fun <reified T> newObjectLongMap() =
    TCollections.synchronizedMap(TObjectLongHashMap<T>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0))
private fun newLongLongMap() =
    TCollections.synchronizedMap(TLongLongHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0, 0))

sealed class Cooldownable(cooldownStrategy: CooldownStrategy) {
    @JvmRecord
    private data class CooldownKey(private val guildId: Long?, private val userId: Long)

    private val userCooldowns = newObjectLongMap<CooldownKey>()
    private val channelCooldowns = newLongLongMap()
    private val guildCooldowns = newLongLongMap()

    private val cooldownMillis: Long  = cooldownStrategy.cooldownMillis
    val cooldownScope: CooldownScope = cooldownStrategy.scope

    fun applyCooldown(event: MessageReceivedEvent) = when (cooldownScope) {
        CooldownScope.USER -> userCooldowns.put(getCooldownKey(event.guild, event.author), System.currentTimeMillis() + cooldownMillis)
        CooldownScope.GUILD -> guildCooldowns.put(event.guild.idLong, System.currentTimeMillis() + cooldownMillis)
        CooldownScope.CHANNEL -> channelCooldowns.put(event.channel.idLong, System.currentTimeMillis() + cooldownMillis)
    }

    fun applyCooldown(event: GenericCommandInteractionEvent) = when (cooldownScope) {
        CooldownScope.USER -> {
            userCooldowns.put(event.toCooldownKey(), System.currentTimeMillis() + cooldownMillis)
        }
        CooldownScope.GUILD -> {
            val guild = event.guild ?: throwInternal("Invalid cooldown scope for DM commands")
            guildCooldowns.put(guild.idLong, System.currentTimeMillis() + cooldownMillis)
        }
        CooldownScope.CHANNEL -> {
            if (!event.isFromGuild) throwInternal("Invalid cooldown scope for DM commands")
            channelCooldowns.put(event.guildChannel.idLong, System.currentTimeMillis() + cooldownMillis)
        }
    }

    fun getCooldown(event: MessageReceivedEvent): Long = when (cooldownScope) {
        CooldownScope.USER -> max(0, userCooldowns[event.toCooldownKey()] - System.currentTimeMillis())
        CooldownScope.GUILD -> max(0, guildCooldowns[event.guild.idLong] - System.currentTimeMillis())
        CooldownScope.CHANNEL -> max(0, channelCooldowns[event.channel.idLong] - System.currentTimeMillis())
    }

    fun getCooldown(event: GenericCommandInteractionEvent): Long = when (cooldownScope) {
        CooldownScope.USER -> {
            max(0, userCooldowns[event.toCooldownKey()] - System.currentTimeMillis())
        }
        CooldownScope.GUILD -> {
            val guild = event.guild ?: throwInternal("Invalid cooldown scope for DM commands")
            max(0, guildCooldowns[guild.idLong] - System.currentTimeMillis())
        }
        CooldownScope.CHANNEL -> {
            if (!event.isFromGuild) throwInternal("Invalid cooldown scope for DM commands")
            max(0, channelCooldowns[event.guildChannel.idLong] - System.currentTimeMillis())
        }
    }

    private fun getCooldownKey(guild: Guild?, user: UserSnowflake) = CooldownKey(guild?.idLong, user.idLong)
    private fun MessageReceivedEvent.toCooldownKey() = getCooldownKey(guild, author)
    private fun GenericCommandInteractionEvent.toCooldownKey() = getCooldownKey(guild, user)
}
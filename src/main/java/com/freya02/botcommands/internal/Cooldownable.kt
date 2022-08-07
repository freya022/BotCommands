package com.freya02.botcommands.internal

import com.freya02.botcommands.api.CooldownScope
import gnu.trove.TCollections
import gnu.trove.impl.Constants
import gnu.trove.map.hash.TObjectLongHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

sealed class Cooldownable(private val context: BContextImpl, cooldownStrategy: CooldownStrategy) {
    @JvmRecord
    private data class CooldownKey(private val placeId: Long?, private val userId: Long)

    //Trove sets are not *always* the fastest, by a small margin, but are by far the most memory efficient
    private val cooldowns =
        TCollections.synchronizedMap(TObjectLongHashMap<CooldownKey>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0))

    private val cooldownMillis: Long = cooldownStrategy.cooldownMillis
    val cooldownScope: CooldownScope = cooldownStrategy.scope

    fun applyCooldown(event: MessageReceivedEvent) = applyCooldown(event.toCooldownKey())

    fun applyCooldown(event: GenericCommandInteractionEvent) = applyCooldown(event.toCooldownKey())

    private fun applyCooldown(cooldownKey: CooldownKey) {
        cooldowns.put(cooldownKey, System.currentTimeMillis() + cooldownMillis)
        context.config.coroutineScopesConfig.cooldownScope.launch {
            delay(cooldownMillis)
            cooldowns.remove(cooldownKey)
        }
    }

    fun getCooldown(event: MessageReceivedEvent) = cooldowns[event.toCooldownKey()] - System.currentTimeMillis()

    fun getCooldown(event: GenericCommandInteractionEvent) = cooldowns[event.toCooldownKey()] - System.currentTimeMillis()

    private fun MessageReceivedEvent.toCooldownKey(): CooldownKey {
        if (!isFromGuild) throwInternal("Invalid cooldown scope for text commands")
        return when (cooldownScope) {
            CooldownScope.USER -> CooldownKey(guild.idLong, author.idLong)
            CooldownScope.GUILD -> CooldownKey(guild.idLong, author.idLong)
            CooldownScope.CHANNEL -> CooldownKey(channel.idLong, author.idLong)
        }
    }

    private fun GenericCommandInteractionEvent.toCooldownKey() = when (cooldownScope) {
        CooldownScope.USER -> {
            CooldownKey(guild?.idLong, user.idLong)
        }

        CooldownScope.GUILD -> {
            val guild = guild ?: throwInternal("Invalid cooldown scope for text commands")
            CooldownKey(guild.idLong, user.idLong)
        }

        CooldownScope.CHANNEL -> {
            if (!isFromGuild) throwInternal("Invalid cooldown scope for text commands")
            CooldownKey(guildChannel.idLong, user.idLong)
        }
    }
}
package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.Cooldownable
import com.freya02.botcommands.internal.throwInternal
import gnu.trove.TCollections
import gnu.trove.impl.Constants
import gnu.trove.map.hash.TObjectLongHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
internal class CooldownService(private val context: BContextImpl) {
    @JvmRecord
    private data class CooldownKey(private val commandPath: CommandPath, private val placeId: Long?, private val userId: Long)

    //Trove sets are not *always* the fastest, by a small margin, but are by far the most memory efficient
    private val cooldowns =
        TCollections.synchronizedMap(TObjectLongHashMap<CooldownKey>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0))

    fun applyCooldown(cooldownable: Cooldownable, event: MessageReceivedEvent) =
        applyCooldown(cooldownable, event.toCooldownKey(cooldownable))

    fun applyCooldown(cooldownable: Cooldownable, event: GenericCommandInteractionEvent) =
        applyCooldown(cooldownable, event.toCooldownKey(cooldownable))

    fun getCooldown(cooldownable: Cooldownable, event: MessageReceivedEvent) = cooldowns[event.toCooldownKey(cooldownable)] - System.currentTimeMillis()

    fun getCooldown(cooldownable: Cooldownable, event: GenericCommandInteractionEvent) = cooldowns[event.toCooldownKey(cooldownable)] - System.currentTimeMillis()

    private fun applyCooldown(cooldownable: Cooldownable, cooldownKey: CooldownKey) {
        cooldowns.put(cooldownKey, System.currentTimeMillis() + cooldownable.cooldownStrategy.cooldownMillis)
        context.coroutineScopesConfig.cooldownScope.launch {
            delay(cooldownable.cooldownStrategy.cooldownMillis)
            cooldowns.remove(cooldownKey)
        }
    }

    private fun MessageReceivedEvent.toCooldownKey(cooldownable: Cooldownable): CooldownKey {
        if (!isFromGuild) throwInternal("Invalid cooldown scope for text commands")
        return when (cooldownable.cooldownStrategy.scope) {
            CooldownScope.USER -> CooldownKey(cooldownable.path, guild.idLong, author.idLong)
            CooldownScope.GUILD -> CooldownKey(cooldownable.path, guild.idLong, author.idLong)
            CooldownScope.CHANNEL -> CooldownKey(cooldownable.path, channel.idLong, author.idLong)
        }
    }

    private fun GenericCommandInteractionEvent.toCooldownKey(cooldownable: Cooldownable) = when (cooldownable.cooldownStrategy.scope) {
        CooldownScope.USER -> CooldownKey(cooldownable.path, guild?.idLong, user.idLong)
        CooldownScope.GUILD -> {
            val guild = guild ?: throwInternal("Invalid cooldown scope for text commands")
            CooldownKey(cooldownable.path, guild.idLong, user.idLong)
        }
        CooldownScope.CHANNEL -> {
            if (!isFromGuild) throwInternal("Invalid cooldown scope for text commands")
            CooldownKey(cooldownable.path, guildChannel.idLong, user.idLong)
        }
    }
}
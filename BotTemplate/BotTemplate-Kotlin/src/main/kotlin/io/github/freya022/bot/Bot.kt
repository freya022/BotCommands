package io.github.freya022.bot

import com.freya02.botcommands.api.core.JDAService
import com.freya02.botcommands.api.core.events.BReadyEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.jdabuilder.light
import io.github.freya022.bot.config.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent

@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> = defaultIntents

    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        // You MUST disable enableCoroutines and set the event manager to the injected one
        light(config.token, intents = intents, enableCoroutines = false) {
            setMaxReconnectDelay(120) //Try to reconnect every 2 minutes instead of the default 15 minutes
            setActivity(Activity.playing("with Kotlin"))
            setEventManager(eventManager)
        }
    }
}
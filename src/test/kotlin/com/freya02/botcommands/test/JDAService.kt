package com.freya02.botcommands.test

import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.annotations.BService
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent

@BService(ServiceStart.READY)
class JDAService(config: Config, eventManager: IEventManager) {
    init {
        light(config.token, enableCoroutines = false) {
            enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
            setActivity(Activity.playing("coroutines go brrr"))
            setEventManager(eventManager)
        }
    }
}
package io.github.freya022.bot

import com.freya02.botcommands.api.core.service.ServiceStart
import com.freya02.botcommands.api.core.service.annotations.BService
import dev.minn.jda.ktx.jdabuilder.light
import io.github.freya022.bot.config.Config
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager

@BService(ServiceStart.READY)
class JDAService(config: Config, manager: IEventManager) { //The same event manager you passed to BBuilder
    init {
        light(config.token, enableCoroutines = false) {
            setMaxReconnectDelay(128)
            setActivity(Activity.playing("with Kotlin"))
            setEventManager(manager)
        }
    }
}
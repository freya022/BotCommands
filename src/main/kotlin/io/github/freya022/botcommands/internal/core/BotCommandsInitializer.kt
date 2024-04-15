package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.internal.core.service.SpringBotCommandsBootstrap
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
internal class BotCommandsInitializer(private val serviceBootstrap: SpringBotCommandsBootstrap) : InitializingBean {
    override fun afterPropertiesSet() = runBlocking {
        serviceBootstrap.runBootstrap()
    }
}
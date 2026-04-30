package io.github.freya022.botcommands.internal.emojis

import io.github.freya022.botcommands.internal.core.service.AbstractBotCommandsBootstrap

internal class InitListener : AbstractBotCommandsBootstrap.Listener {

    override fun onInit() {
        AppEmojisLoader.clear()
    }
}

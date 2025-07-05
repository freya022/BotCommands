package io.github.freya022.botcommands.framework.utils

import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.framework.FakeBot

fun BotCommands.createTest(
    textCommands: Boolean = false,
    appCommands: Boolean = false,
    components: Boolean = false,
    modals: Boolean = false,
    appEmojis: Boolean = false,
    builder: BConfigBuilder.() -> Unit
) = create {
    disableExceptionsInDMs = true

    addClass<FakeBot>()

    textCommands {
        enable = textCommands
    }

    applicationCommands {
        enable = appCommands
    }

    components {
        enable = components
    }

    modals {
        enable = modals
    }

    appEmojis {
        enable = appEmojis
    }

    builder()
}
package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_

private const val TEST_BUTTON_BUTTON_LISTENER_NAME = "MyJavaCommand: TestButton"

class MyButton : ApplicationCommand() {
    @CommandMarker
    fun onSlashButton(event: GlobalSlashEvent, components: Components) {
        val lol = components.primaryButton {
            it.reply_("Button pressed", ephemeral = true).queue()
        }.build("Test")

        event.reply_(
            "Buttons !",
            ephemeral = true,
            components = listOf(row(lol))
        ).queue()
    }

    @JDAButtonListener(name = TEST_BUTTON_BUTTON_LISTENER_NAME)
    fun onTestButtonClick(event: ButtonEvent) {

    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand(CommandPath.of("button")) {
            customOption("components")

            function = ::onSlashButton
        }
    }
}
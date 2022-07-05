package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.components.event.ButtonEvent

private const val TEST_BUTTON_BUTTON_LISTENER_NAME = "MyJavaCommand: TestButton"

class MyButton : ApplicationCommand() {
    @CommandMarker
    fun onSlashButton(event: GlobalSlashEvent) {

    }

    @JDAButtonListener(name = TEST_BUTTON_BUTTON_LISTENER_NAME)
    fun onTestButtonClick(event: ButtonEvent) {

    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand(CommandPath.of("button")) {
            function = ::onSlashButton
        }
    }
}
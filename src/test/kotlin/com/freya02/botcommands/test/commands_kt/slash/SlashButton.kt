package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.annotations.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import java.util.concurrent.TimeUnit

private const val TEST_BUTTON_BUTTON_LISTENER_NAME = "MyButton: TestButton"

@CommandMarker
class SlashButton : ApplicationCommand() {
    @JDASlashCommand(name = "button_annotated")
    fun onSlashButton(event: GuildSlashEvent, components: Components) {
        val ephemeral = components
            .primaryButton {
                it.reply_("Button pressed", ephemeral = true).queue()
            }
            .timeout(5, TimeUnit.SECONDS) { event.hook.editOriginalComponents().queue() }
            .addUsers(event.user)
            .build("Test ephemeral")

        val persistent = components.primaryButton(TEST_BUTTON_BUTTON_LISTENER_NAME, "Custom content")
            .timeout(5, TimeUnit.SECONDS)
            .addUsers(event.user)
            .build("Test persistent")

        event.reply_(
            "Buttons !",
//            ephemeral = true,
            components = listOf(row(ephemeral, persistent))
        ).queue()
    }

    @JDAButtonListener(name = TEST_BUTTON_BUTTON_LISTENER_NAME)
    suspend fun onTestButtonClick(event: ButtonEvent, content: String) {
        event.reply_(content, ephemeral = true).await()
    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand(CommandPath.of("button")) {
            customOption("components")

            function = ::onSlashButton
        }
    }
}
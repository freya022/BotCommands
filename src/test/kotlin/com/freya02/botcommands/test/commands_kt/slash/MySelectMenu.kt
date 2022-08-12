package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.components.annotations.JDASelectionMenuListener
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.SelectionEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import java.util.concurrent.TimeUnit

private const val TEST_SELECTION_SELECTION_LISTENER_NAME = "MySelectMenu: TestSelection"

@CommandMarker
class MySelectMenu {
    @CommandMarker
    fun onSlashSelectMenu(event: GlobalSlashEvent, components: Components) {
        val ephemeral = components
            .selectionMenu {
                it.reply_("Selection: ${it.values}", ephemeral = true).queue()
            }
            .timeout(10, TimeUnit.SECONDS) { event.hook.editOriginalComponents().queue() }
            .addUsers(event.user)
            .setPlaceholder("Ephemeral menu")
            .addOption("Choice 1", "42")
            .addOption("Choice 2", "90")
            .build()

        val persistent = components.selectionMenu(TEST_SELECTION_SELECTION_LISTENER_NAME, "Custom content")
            .timeout(10, TimeUnit.SECONDS)
            .addUsers(event.user)
            .setPlaceholder("Persistent menu")
            .addOption("Choice 1 1", "67")
            .addOption("Choice 1 2", "91")
            .build()

        event.reply_(
            "Select menus !",
//            ephemeral = true,
            components = listOf(row(ephemeral), row(persistent))
        ).queue()
    }

    @JDASelectionMenuListener(name = TEST_SELECTION_SELECTION_LISTENER_NAME)
    suspend fun onTestSelectionClick(event: SelectionEvent, content: String) {
        event.reply_(content + ": ${event.values}", ephemeral = true).await()
    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand(CommandPath.of("selectmenu")) {
            customOption("components")

            function = ::onSlashSelectMenu
        }
    }
}
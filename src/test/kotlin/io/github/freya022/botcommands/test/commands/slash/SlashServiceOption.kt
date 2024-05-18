package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.shortTextInput
import net.dv8tion.jda.api.JDA
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Command
class SlashServiceOption : ApplicationCommand() {
    @JDASlashCommand(name = "service_option")
    suspend fun onSlashServiceOption(
        event: GuildSlashEvent,
        buttons: Buttons,
        @SlashOption option: String,
        @LocalizationBundle("MyCommands") localizationContext: AppLocalizationContext,
    ) {
        val button = buttons.primary("Click me").persistent {
            timeout(5.seconds, "SlashServiceOption: button", Random.nextDouble())
            bindTo("SlashServiceOption: button", option, 1)
        }

        event.reply_(components = button.into(), ephemeral = true).await()
    }

    @ComponentTimeoutHandler("SlashServiceOption: button")
    fun onButtonTimeout(
        data: ComponentTimeoutData,
        @TimeoutData randomNumber: Double,
        jda: LazyService<JDA>,
    ) {
        if (jda.canCreateService()) {
            println("Deleting old components (not really tho)")
        } else {
            println("Missing JDA, cannot delete old components")
        }
    }

    @JDAButtonListener("SlashServiceOption: button")
    suspend fun onButtonClick(
        event: ButtonEvent,
        @ComponentData slashInput: String,
        @ComponentData randomNum: Double,
        modals: Modals,
        // Not supported yet
//        @LocalizationBundle("MyCommands") localizationContext: AppLocalizationContext,
    ) {
        val modal = modals.create("Title") {
            shortTextInput("input", "Sample text")

            bindTo("SlashServiceOption: modal", slashInput, randomNum, Random.nextDouble())
        }

        event.replyModal(modal).await()
    }

    @ModalHandler("SlashServiceOption: modal")
    suspend fun onModalSubmit(
        event: ModalEvent,
        @ModalData slashInput: String,
        @ModalData buttonRandomNum: Double,
        @ModalData randomNum: Double,
        @ModalInput("input") input: String,
        service: ServiceContainer,
        @LocalizationBundle("MyCommands") localizationContext: AppLocalizationContext,
    ) {
        event.reply_(
            """
                Slash command option: $slashInput
                Button random number: $buttonRandomNum
                Random number: $randomNum
                Input: $input
            """.trimIndent(),
            ephemeral = true
        ).await()
    }
}
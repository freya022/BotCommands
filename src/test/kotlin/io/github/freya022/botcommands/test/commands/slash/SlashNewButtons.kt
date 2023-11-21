package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.and
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.filter
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.or
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.test.filters.InVoiceChannel
import io.github.freya022.botcommands.test.filters.IsBotOwner
import io.github.freya022.botcommands.test.filters.IsGuildOwner
import io.github.freya022.botcommands.test.switches.TestServiceChecker
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Command
@Dependencies(Components::class)
class SlashNewButtons(serviceContainer: ServiceContainer) : ApplicationCommand() {
    private val components: Components by serviceContainer.lazy()

    @JDASlashCommand(name = "new_buttons")
    suspend fun onSlashNewButtons(event: GuildSlashEvent) {
        val persistentButton = persistentGroupTest(event)
        val ephemeralButton = ephemeralGroupTest(event)
        val row = buildList {
            this += persistentButton
            this += ephemeralButton
            this += noGroupButton(event)
            if (TestServiceChecker.useTestServices) {
                this += filteredButton()
            }
        }.into()

        event.reply("OK, button ID: ${persistentButton.id}").setComponents(row).queue()

        try {
//            withTimeout(5.seconds) {
                val buttonEvent: ButtonEvent = ephemeralButton.await()
                event.hook.send("Done awaiting !", ephemeral = true).queue()
//            }
        } catch (e: TimeoutCancellationException) {
            event.hook.send("Too slow", ephemeral = true).queue()
        }
    }

    private fun filteredButton() = components.ephemeralButton(ButtonStyle.DANGER, "Leave VC") {
        filters += (filter<IsBotOwner>() or filter<IsGuildOwner>()) and filter<InVoiceChannel>()
        bindTo {
            it.guild!!.kickVoiceMember(it.member!!).await()
            it.deferEdit().await()
        }
    }

    private fun noGroupButton(event: GuildSlashEvent) =
        components.ephemeralButton(ButtonStyle.DANGER, "Delete") {
            oneUse = true
            bindTo { event.hook.deleteOriginal().queue() }
            timeout(5.seconds)
        }

    private suspend fun persistentGroupTest(event: GuildSlashEvent): Button {
        val firstButton = components.persistentButton(ButtonStyle.PRIMARY, "Persistent") {
            oneUse = true //Cancels whole group if used
            addUserIds(1234L)
            constraints += Permission.ADMINISTRATOR
            bindTo(PERSISTENT_BUTTON_LISTENER_NAME, ThreadLocalRandom.current().nextDouble(), event.member, null)
        }

        val secondButton = components.persistentButton(ButtonStyle.PRIMARY, "Invisible") {
            oneUse = true //Cancels whole group if used
            addUserIds(1234L)
            constraints += Permission.ADMINISTRATOR
            bindTo(PERSISTENT_BUTTON_LISTENER_NAME, ThreadLocalRandom.current().nextDouble(), event.member, null)
        }

        components.newPersistentGroup(firstButton, secondButton) {
            timeout(10.seconds, PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME, null)
        }
        return firstButton
    }

    private suspend fun ephemeralGroupTest(event: GuildSlashEvent): Button {
        val firstButton = components.ephemeralButton(ButtonStyle.SECONDARY, "Ephemeral") {
            oneUse = true //Cancels whole group if used
            addUserIds(1234L)
            constraints += Permission.ADMINISTRATOR
            bindTo { evt -> evt.reply_("Ephemeral button clicked", ephemeral = true).queue() }
        }

        components.newEphemeralGroup(firstButton) {
            timeout(15.minutes) {
                event.hook.retrieveOriginal()
                    .flatMap { event.hook.editOriginalComponents(it.components.asDisabled()) }
                    .queue()
            }
        }
        return firstButton
    }

    @JDAButtonListener(name = PERSISTENT_BUTTON_LISTENER_NAME)
    fun onFirstButtonClicked(event: ButtonEvent, double: Double, inputUser: InputUser, nullValue: Member?) {
        event.reply_("Persistent button clicked, double: $double, member: ${inputUser.asTag}, null: $nullValue", ephemeral = true).queue()
    }

    @ComponentTimeoutHandler(name = PERSISTENT_BUTTON_TIMEOUT_LISTENER_NAME)
    fun onFirstButtonTimeout(data: ComponentTimeoutData) {
        println(data)
    }

    @GroupTimeoutHandler(name = PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME)
    fun onFirstGroupTimeout(data: GroupTimeoutData, nullObj: String?) {
        println("$data : $nullObj")
    }

    companion object {
        private const val PERSISTENT_BUTTON_LISTENER_NAME = "SlashNewButtons: persistentButton"
        private const val PERSISTENT_BUTTON_TIMEOUT_LISTENER_NAME = "SlashNewButtons: persistentButtonTimeout"
        private const val PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME = "SlashNewButtons: persistentGroupTimeout"
    }
}
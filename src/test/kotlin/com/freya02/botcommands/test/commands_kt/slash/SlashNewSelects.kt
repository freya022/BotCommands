package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.EntitySelectMenu
import com.freya02.botcommands.api.components.StringSelectMenu
import com.freya02.botcommands.api.components.annotations.ComponentTimeoutHandler
import com.freya02.botcommands.api.components.annotations.GroupTimeoutHandler
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener
import com.freya02.botcommands.api.components.data.ComponentTimeoutData
import com.freya02.botcommands.api.components.data.GroupTimeoutData
import com.freya02.botcommands.api.components.event.StringSelectEvent
import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Duration.Companion.seconds

@CommandMarker
class SlashNewSelects(private val components: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "new_selects")
    suspend fun onSlashNewButtons(event: GuildSlashEvent) {
        val persistentSelect = persistentGroupTest(event)
        val ephemeralSelect = ephemeralGroupTest(event)

        //These *should* be able to store continuations and throw a TimeoutException once the timeout is met
//        val groupEvent: GenericComponentInteractionCreateEvent = firstGroup.await()
//        val buttonEvent: ButtonEvent = firstButton.await()

        event.replyComponents(row(persistentSelect), row(ephemeralSelect)).queue()
    }

    private suspend fun persistentGroupTest(event: GuildSlashEvent): StringSelectMenu {
        val firstSelect = components.persistentStringSelectMenu {
            oneUse = true //Cancels whole group if used
            constraints {
                addUserIds(1234L)
                permissions += Permission.ADMINISTRATOR
            }
            bindTo(PERSISTENT_SELECT_LISTENER_NAME, ThreadLocalRandom.current().nextDouble(), event.member)

            addOption("Test", "Test")
            addOption("Foo", "Foo")
            addOption("Bar", "Bar")
        }

        val secondSelect = components.persistentStringSelectMenu {
            oneUse = true //Cancels whole group if used
            constraints {
                addUserIds(1234L)
                permissions += Permission.ADMINISTRATOR
            }
            bindTo(PERSISTENT_SELECT_LISTENER_NAME, ThreadLocalRandom.current().nextDouble(), event.member)

            addOption("Test", "Test")
            addOption("Foo", "Foo")
            addOption("Bar", "Bar")
        }

        components.newPersistentGroup(firstSelect, secondSelect) {
            timeout(10.seconds, PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME)
        }
        return firstSelect
    }

    private suspend fun ephemeralGroupTest(event: GuildSlashEvent): EntitySelectMenu {
        val firstSelect = components.ephemeralEntitySelectMenu(SelectTarget.ROLE) {
            oneUse = true //Cancels whole group if used
            constraints {
                addUserIds(1234L)
                permissions += Permission.ADMINISTRATOR
            }
            bindTo { evt -> evt.reply_("Ephemeral select menu clicked", ephemeral = true).queue() }
        }

        components.newEphemeralGroup(firstSelect) {
            timeout(15.seconds) {
                event.hook.retrieveOriginal()
                    .flatMap { event.hook.editOriginalComponents(it.components.asDisabled()) }
                    .queue()
            }
        }
        return firstSelect
    }

    @JDASelectMenuListener(name = PERSISTENT_SELECT_LISTENER_NAME)
    fun onFirstSelectClicked(event: StringSelectEvent) {
        event.reply_("Persistent select menu clicked", ephemeral = true).queue()
    }

    @ComponentTimeoutHandler(name = PERSISTENT_SELECT_TIMEOUT_LISTENER_NAME)
    fun onFirstSelectTimeout(data: ComponentTimeoutData) {
        println(data)
    }

    @GroupTimeoutHandler(name = PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME)
    fun onFirstGroupTimeout(data: GroupTimeoutData) {
        println(data)
    }

    companion object {
        private const val PERSISTENT_SELECT_LISTENER_NAME = "SlashNewSelects: persistentSelect"
        private const val PERSISTENT_SELECT_TIMEOUT_LISTENER_NAME = "SlashNewSelects: persistentSelectTimeout"
        private const val PERSISTENT_GROUP_TIMEOUT_LISTENER_NAME = "SlashNewSelects: persistentGroupTimeout"
    }
}
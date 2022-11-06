package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.new_components.ComponentGroup
import com.freya02.botcommands.api.new_components.NewComponents
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

private const val FIRST_BUTTON_LISTENER_NAME = "SlashNewButtons: firstButton"

@CommandMarker
class SlashNewButtons : ApplicationCommand() {
    @JDASlashCommand(name = "new_buttons")
    suspend fun onSlashNewButtons(event: GuildSlashEvent, components: NewComponents) {
        val firstButton: Button = components.primaryButton()
            .oneUse() //Cancels whole group if used
            .constraints {
                addUserIds(1234L)
                permissions += Permission.ADMINISTRATOR
            }
            .timeout(20, TimeUnit.SECONDS) //Incompatible with group, emit warn when built
//            .timeout(10, TimeUnit.SECONDS, "SlashNewButtons: onFirstButtonTimeout"/* no params */)
            .bindTo(FIRST_BUTTON_LISTENER_NAME, ThreadLocalRandom.current().nextDouble(), event.member)
//            .bindTo { evt -> TODO() }
            .build("test")

        val firstGroup: ComponentGroup = components.newGroup(true, 10, TimeUnit.SECONDS, firstButton)

        //These *should* be able to store continuations and throw a TimeoutException once the timeout is met
//        val groupEvent: GenericComponentInteractionCreateEvent = firstGroup.await()
//        val buttonEvent: ButtonEvent = firstButton.await()

        event.reply("OK, button ID: ${firstButton.id}")
            .addActionRow(firstButton)
            .queue()
    }

    @JDAButtonListener(name = FIRST_BUTTON_LISTENER_NAME)
    fun onFirstButtonClicked(event: ButtonEvent) {
        event.reply_("Button clicked", ephemeral = true).queue()
    }
}
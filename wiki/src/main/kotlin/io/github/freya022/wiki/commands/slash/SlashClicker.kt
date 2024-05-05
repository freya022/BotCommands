package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.time.Duration.Companion.days

// Exists only for @TopLevelSlashCommandData
@Command
class SlashDummyClicker : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "dummy")
    @TopLevelSlashCommandData
    fun onSlashClicker(event: GuildSlashEvent) {
        event.reply_("Unused", ephemeral = true).queue()
    }
}

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:persistent-clicker-kotlin]
@Command
class SlashPersistentClicker(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "persistent", description = "Creates a button you can infinitely click")
    suspend fun onSlashClicker(event: GuildSlashEvent) {
        val button = createButton(event, count = 0)
        event.replyComponents(row(button)).await()
    }

    // The name should be unique,
    // I recommend naming the handler "[ClassName]: [purpose]"
    // And the name would be "on[purpose]Click"
    @JDAButtonListener("SlashPersistentClicker: cookie")
    suspend fun onCookieClick(event: ButtonEvent, count: Int) {
        val button = createButton(event, count + 1)
        event.editComponents(row(button)).await()
    }

    // Same thing here, names don't collide with other types of listener
    @ComponentTimeoutHandler("SlashPersistentClicker: cookie")
    fun onCookieTimeout(timeout: ComponentTimeoutData, count: String) {
        println("User finished clicking $count cookies")
    }

    private suspend fun createButton(event: Interaction, count: Int): Button {
        // Create a primary-styled button
        return buttons.primary("$count cookies")
            // Sets the emoji on the button,
            // this can be an unicode emoji, an alias or even a custom emoji
            .withEmoji("cookie")

            // Create a button that can be used even after a restart
            .persistent {
                // Make it so this button is only usable once
                // this is not an issue as we recreate the button everytime.
                // If this wasn't usable only once, the timeout would run for each button.
                oneUse = true

                // Only allow the caller to use the button
                constraints += event.user

                // Timeout and call the method after the button hasn't been used for a day
                // The timeout gets cancelled if the button is invalidated
                timeout(1.days, handlerName = "SlashPersistentClicker: cookie", count)

                // When clicked, run the onCookieClick method with the count
                // Extension for type-safe binding, no need to type the name
                bindTo(::onCookieClick, count)
            }
    }
}
// --8<-- [end:persistent-clicker-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:ephemeral-clicker-kotlin]
@Command
class SlashEphemeralClicker(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "clicker", subcommand = "ephemeral", description = "Creates a button you can click until the bot restarts")
    suspend fun onSlashClicker(event: GuildSlashEvent) {
        val button = createButton(event, count = 0)
        event.replyComponents(row(button)).await()
    }

    private suspend fun createButton(event: Interaction, count: Int): Button {
        // Create a primary-styled button
        return buttons.primary("$count cookies")
            // Sets the emoji on the button,
            // this can be an unicode emoji, an alias or even a custom emoji
            .withEmoji("cookie")

            // Create a button that can be used until the bot restarts
            .ephemeral {
                // Make it so this button is only usable once
                // this is not an issue as we recreate the button everytime.
                // If this wasn't usable only once, the timeout would run for each button.
                oneUse = true

                // Only allow the caller to use the button
                constraints += event.user

                // Run this callback after the button hasn't been used for a day
                // The timeout gets cancelled if the button is invalidated
                timeout(1.days) {
                    println("User finished clicking $count cookies")
                }

                // When clicked, run this callback
                bindTo { buttonEvent ->
                    val newButton = createButton(buttonEvent, count + 1)
                    buttonEvent.editComponents(row(newButton)).await()
                }
            }
    }
}
// --8<-- [end:ephemeral-clicker-kotlin]
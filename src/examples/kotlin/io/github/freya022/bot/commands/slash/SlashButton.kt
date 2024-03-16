package io.github.freya022.bot.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val buttonListenerName = "SlashButton: persistentButton" //ClassName: theButtonPurpose

@Command
class SlashButton(private val componentsService: Components) : ApplicationCommand() {
    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(name = "button", description = "Try out the new buttons!")
    suspend fun onSlashButton(event: GlobalSlashEvent) {
        val components: MutableList<Button> = arrayListOf()
        components += componentsService.button(ButtonStyle.PRIMARY, "Click me under 5 seconds").ephemeral {
            timeout(5.seconds) {
                event.hook.editOriginalComponents(components.map(Button::asDisabled).row()).queue()
            }
            bindTo { buttonEvent ->
                buttonEvent.editButton(buttonEvent.button.asDisabled()).await() // Coroutines!
            }
        }

        components += componentsService.button(ButtonStyle.SECONDARY, "Click me anytime").persistent {
            bindTo(buttonListenerName)
//            bindTo(::onPersistentButtonClick) //Also works
        }

        event.replyComponents(components.into())
            .setContent(TimeFormat.RELATIVE.format(Instant.now() + 5.seconds.toJavaDuration()))
            .setEphemeral(true)
            .queue()
    }

    @JDAButtonListener(name = buttonListenerName)
    suspend fun onPersistentButtonClick(event: ButtonEvent) {
        event.editButton(event.button.asDisabled()).await()
    }
}
package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.components.row
import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.jda.emojis.unicode.Emojis
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.timeoutWith
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableTimeoutData
import kotlin.time.Duration.Companion.seconds

data class MyComponentData(
    val userName: String,
    val nested: Nested,
) {

    data class Nested(
        val roleNames: List<String>,
    )
}

@Command
class SlashComponentData(
    private val buttons: Buttons,
) : ApplicationCommand() {

    @JDASlashCommand(name = "component_data")
    suspend fun onSlashComponentData(event: GuildSlashEvent) {
        val data = MyComponentData(
            userName = event.member.effectiveName,
            MyComponentData.Nested(
                roleNames = event.member.roles.map { it.name }
            )
        )
        val button = buttons.primary("See your data at the time of sending", Emojis.PENCIL).persistent {
            bindWith(SlashComponentData::onSeeDataClicked, data)
            timeoutWith(15.seconds, ::onSeeDataTimeout, data)
        }

        event.replyComponents(row(button)).setEphemeral(true).queue()
    }

    @JDAButtonListener
    fun onSeeDataClicked(event: ButtonEvent, @SerializableComponentData data: MyComponentData) {
        val message = """
            You had the following attributes:
            - User name: ${data.userName}
            - Role names: ${data.nested.roleNames.joinToString(", ")}
        """.trimIndent()
        event.reply_(message, ephemeral = true).queue()
    }

    @ComponentTimeoutHandler
    fun onSeeDataTimeout(event: ComponentTimeoutData, @SerializableTimeoutData data: MyComponentData) {
        println("Component expired with $data")
    }
}

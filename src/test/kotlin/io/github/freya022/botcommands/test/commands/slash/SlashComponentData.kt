package io.github.freya022.botcommands.test.commands.slash

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.freya02.jda.emojis.unicode.Emojis
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

data class MyComponentData(
    val userName: String,
    val nested: Nested,
) {

    data class Nested(
        val roleNames: List<String>,
    )
}

@Resolver
class MyComponentDataResolver :
        ClassParameterResolver<MyComponentDataResolver, MyComponentData>(MyComponentData::class),
        ComponentParameterResolver<MyComponentDataResolver, MyComponentData> {

    private val mapper = jacksonObjectMapper()

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): MyComponentData {
        return mapper.readValue(arg, MyComponentData::class.java)
    }

    override fun serialize(obj: MyComponentData): String {
        return mapper.writeValueAsString(obj)
    }
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
        }

        event.replyComponents(row(button)).setEphemeral(true).queue()
    }

    @JDAButtonListener
    fun onSeeDataClicked(event: ButtonEvent, @ComponentData data: MyComponentData) {
        val message = """
            You had the following attributes:
            - User name: ${data.userName}
            - Role names: ${data.nested.roleNames.joinToString(", ")}
        """.trimIndent()
        event.reply_(message, ephemeral = true).queue()
    }
}
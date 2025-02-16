package io.github.freya022.botcommands.test.commands.slash

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.freya02.jda.emojis.unicode.Emojis
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.timeoutWith
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.time.Duration.Companion.seconds

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
        ComponentParameterResolver<MyComponentDataResolver, MyComponentData>,
        TimeoutParameterResolver<MyComponentDataResolver, MyComponentData> {

    private val mapper = jacksonObjectMapper()

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData
    ): MyComponentData {
        return mapper.readValue<MyComponentData>(data.asBytes())
    }

    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): MyComponentData {
        return mapper.readValue<MyComponentData>(data.asBytes())
    }

    override fun serialize(obj: MyComponentData): SerializedComponentData {
        return SerializedComponentData.fromBytes(mapper.writeValueAsBytes(obj))
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
            timeoutWith(15.seconds, ::onSeeDataTimeout, data)
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

    @ComponentTimeoutHandler
    fun onSeeDataTimeout(event: ComponentTimeoutData, @TimeoutData data: MyComponentData) {
        println("Component expired with $data")
    }
}
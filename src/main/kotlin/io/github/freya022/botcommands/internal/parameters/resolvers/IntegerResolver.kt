package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
class IntegerResolver : ClassParameterResolver<IntegerResolver, Int>(Int::class),
                        TextParameterResolver<IntegerResolver, Int>,
                        SlashParameterResolver<IntegerResolver, Int>, ComponentParameterResolver<IntegerResolver, Int>,
                        TimeoutParameterResolver<IntegerResolver, Int> {

    override val pattern: Pattern = Pattern.compile("(\\d+)")
    override val testExample: String = "1234"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "42"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Int? = args[0]!!.toIntOrNull()


    override val optionType: OptionType = OptionType.INTEGER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Int? {
        return try {
            optionMapping.asInt
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0
        }
    }


    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData
    ): Int = data.asString().toInt()

    override fun serialize(obj: Int) = SerializedComponentData.fromString(obj.toString())


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Int = data.asString().toInt()
}

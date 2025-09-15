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
import io.github.freya022.botcommands.api.parameters.resolvers.QuotableTextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
class StringResolver : ClassParameterResolver<StringResolver, String>(String::class),
                       QuotableTextParameterResolver<StringResolver, String>,
                       SlashParameterResolver<StringResolver, String>,
                       ComponentParameterResolver<StringResolver, String>,
                       TimeoutParameterResolver<StringResolver, String> {

    override val pattern: Pattern = Pattern.compile("(.+)")
    override val quotedPattern: Pattern = Pattern.compile("\"(.+)\"")
    override val testExample: String = "foobar"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "foo bar"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): String? = args[0]


    override val optionType: OptionType
        get() = OptionType.STRING

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): String = optionMapping.asString


    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData
    ): String = data.asString()

    override fun serialize(obj: String) = SerializedComponentData.fromString(obj)


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): String = data.asString()
}

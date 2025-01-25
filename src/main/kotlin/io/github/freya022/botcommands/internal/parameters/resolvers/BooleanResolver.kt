package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.components.options.ComponentOption
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
class BooleanResolver : ClassParameterResolver<BooleanResolver, Boolean>(Boolean::class),
                        TextParameterResolver<BooleanResolver, Boolean>,
                        SlashParameterResolver<BooleanResolver, Boolean>,
                        ComponentParameterResolver<BooleanResolver, Boolean>,
                        TimeoutParameterResolver<BooleanResolver, Boolean> {

    override val pattern: Pattern = Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE)
    override val testExample: String = "true"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "true"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Boolean? = parseBoolean(args[0]!!)


    override val optionType: OptionType get() = OptionType.BOOLEAN

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Boolean = optionMapping.asBoolean


    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): Boolean? = parseBoolean(arg)


    override suspend fun resolveSuspend(option: TimeoutOption, arg: String): Boolean? = parseBoolean(arg)


    private fun parseBoolean(arg: String): Boolean? {
        return if (arg.equals("false", ignoreCase = true)) {
            false
        } else if (arg.equals("true", ignoreCase = true)) {
            true
        } else {
            null
        }
    }
}

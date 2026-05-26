package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@ServiceName("textCommandBooleanResolver")
@RequiresTextCommands
internal class BooleanResolver : ClassParameterResolver<BooleanResolver, Boolean>(Boolean::class),
                                 TextParameterResolver<BooleanResolver, Boolean> {

    override val pattern: Pattern = Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE)
    override val testExample: String = "true"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "true"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): Boolean? {
        val arg = args[0]!!
        return if (arg.equals("false", ignoreCase = true)) {
            false
        } else if (arg.equals("true", ignoreCase = true)) {
            true
        } else {
            null
        }
    }
}

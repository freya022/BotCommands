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
@ServiceName("textCommandDoubleResolver")
@RequiresTextCommands
internal class DoubleResolver : ClassParameterResolver<DoubleResolver, Double>(Double::class),
                                TextParameterResolver<DoubleResolver, Double> {

    override val pattern: Pattern = Pattern.compile("([-+]?[0-9]*[.,]?[0-9]+)")
    override val testExample: String = "1234.42"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "3.14159"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): Double? = args[0]!!.toDoubleOrNull()
}

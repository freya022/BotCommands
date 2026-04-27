package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.QuotableTextParameterResolver
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@ServiceName("textCommandStringResolver")
internal class StringResolver : ClassParameterResolver<StringResolver, String>(String::class),
                                QuotableTextParameterResolver<StringResolver, String> {

    override val pattern: Pattern = Pattern.compile("(.+)")
    override val quotedPattern: Pattern = Pattern.compile("\"(.+)\"")
    override val testExample: String = "foobar"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "foo bar"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): String? = args[0]
}

package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@ServiceName("textCommandGuildResolver")
internal class GuildResolver : ClassParameterResolver<GuildResolver, Guild>(Guild::class),
                               TextParameterResolver<GuildResolver, Guild> {

    override val pattern: Pattern = Pattern.compile("(\\d+)")
    override val testExample: String = "1234"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = event.guild.id

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): Guild? = event.jda.getGuildById(args[0]!!)
}

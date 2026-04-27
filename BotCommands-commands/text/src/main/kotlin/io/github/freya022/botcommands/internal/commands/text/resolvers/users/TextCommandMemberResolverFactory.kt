package io.github.freya022.botcommands.internal.commands.text.resolvers.users

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@ResolverFactory
internal class TextCommandMemberResolverFactory(
    private val resolver: TextCommandInputUserResolver,
) : TypedParameterResolverFactory(Member::class) {

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        inferSupportedResolversFrom(resolver.javaClass)

    private val adapter = Adapter()

    override fun get(request: ResolverRequest): IParameterResolver<*> {
        request.checkGuildOnly(Member::class)
        return adapter
    }

    private inner class Adapter : ClassParameterResolver<Adapter, Member>(Member::class),
                                  TextParameterResolver<Adapter, Member> {

        override val pattern: Pattern get() = resolver.pattern
        override val testExample: String get() = resolver.testExample

        override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
            return resolver.getHelpExample(option, event)
        }

        override suspend fun resolveSuspend(
            option: TextCommandOption,
            event: MessageReceivedEvent,
            args: Array<String?>,
        ): Member? {
            return resolver.resolveSuspend(option, event, args)?.member
        }
    }
}

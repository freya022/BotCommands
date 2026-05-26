package io.github.freya022.botcommands.internal.commands.application.resolvers.users

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@ResolverFactory
@RequiresApplicationCommands
internal class UserContextCommandMemberResolverFactory(
    private val resolver: UserContextCommandInputUserResolver,
) : TypedParameterResolverFactory(Member::class) {

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        inferSupportedResolversFrom(resolver.javaClass)

    private val adapter = Adapter()

    override fun get(request: ResolverRequest): IParameterResolver<*> {
        request.checkGuildOnly(Member::class)
        return adapter
    }

    private inner class Adapter : ClassParameterResolver<Adapter, Member>(Member::class),
                                  UserContextParameterResolver<Adapter, Member> {

        override fun resolve(option: UserContextCommandOption, event: UserContextInteractionEvent): Member? {
            return resolver.resolve(option, event).member
        }
    }
}

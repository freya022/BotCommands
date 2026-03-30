package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.entities.asInputUser
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@ResolverFactory
internal class ComponentMemberResolverFactory(
    private val resolver: ComponentInputUserResolver,
) : TypedParameterResolverFactory(Member::class) {

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        inferSupportedResolversFrom(resolver.javaClass)

    private val adapter = Adapter()

    override fun get(request: ResolverRequest): IParameterResolver<*> {
        request.checkGuildOnly(Member::class)
        return adapter
    }

    private inner class Adapter : ClassParameterResolver<Adapter, Member>(Member::class),
                                  ComponentParameterResolver<Adapter, Member> {

        override fun serialize(obj: Member): SerializedComponentData {
            return resolver.serialize(obj.asInputUser())
        }

        override suspend fun resolveSuspend(
            option: ComponentOption,
            event: GenericComponentInteractionCreateEvent,
            data: SerializedComponentData,
        ): Member? {
            return resolver.resolveSuspend(option, event, data)?.member
        }
    }
}

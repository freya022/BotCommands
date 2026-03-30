package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@ResolverFactory
internal class SlashCommandMemberResolverFactory(
    private val resolver: SlashCommandInputUserResolver,
) : TypedParameterResolverFactory(Member::class) {

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> =
        inferSupportedResolversFrom(resolver.javaClass)

    private val adapter = Adapter()

    override fun get(request: ResolverRequest): IParameterResolver<*> {
        request.checkGuildOnly(Member::class)
        return adapter
    }

    private inner class Adapter : ClassParameterResolver<Adapter, Member>(Member::class),
                                  SlashParameterResolver<Adapter, Member> {

        override val optionType: OptionType = resolver.optionType

        override fun resolve(
            option: SlashCommandOption,
            event: CommandInteractionPayload,
            optionMapping: OptionMapping,
        ): Member? {
            return resolver.resolve(option, event, optionMapping).member
        }
    }
}

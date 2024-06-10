package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.TypedParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.typeOf

object ListResolver :
    TypedParameterResolver<ListResolver, List<Double>>(typeOf<List<Double>>()),
    SlashParameterResolver<ListResolver, List<Double>> {

    override val optionType: OptionType = OptionType.NUMBER

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): List<Double> = listOf(optionMapping.asDouble * 2)
}

@ResolverFactory
object ListResolverFactory : TypedParameterResolverFactory<ListResolver>(ListResolver::class, typeOf<List<Double>>()) {
    override fun get(request: ResolverRequest): ListResolver = ListResolver
}
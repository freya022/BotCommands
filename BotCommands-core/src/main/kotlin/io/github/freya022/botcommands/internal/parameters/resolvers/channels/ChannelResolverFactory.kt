package io.github.freya022.botcommands.internal.parameters.resolvers.channels

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@ResolverFactory
internal class ChannelResolverFactory(override val context: BContext) : AbstractChannelResolverFactory() {
    internal class ChannelResolver(
        context: BContext,
        private val type: Class<out GuildChannel>,
        override val channelTypes: Set<ChannelType>
    ) : AbstractChannelResolver<ChannelResolver>(context),
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        IChannelResolver {

        //region Slash
        override val optionType: OptionType = OptionType.CHANNEL

        override suspend fun resolveSuspend(
            option: SlashCommandOption,
            event: CommandInteractionPayload,
            optionMapping: OptionMapping
        ): GuildChannel {
            val channel = optionMapping.asChannel
            if (type.isInstance(channel)) {
                return type.cast(channel)
            } else {
                throwInternal("A ${optionMapping.channelType} channel option could not be cast into ${type.simpleNestedName}, channel: $channel")
            }
        }
        //endregion
    }

    override fun getResolverType(): Class<out IParameterResolver<*>> = ChannelResolver::class.java

    override fun createResolver(
        context: BContext,
        erasure: Class<out GuildChannel>,
        channelTypes: Set<ChannelType>,
    ): IParameterResolver<*> {
        return ChannelResolver(context, erasure, channelTypes)
    }
}

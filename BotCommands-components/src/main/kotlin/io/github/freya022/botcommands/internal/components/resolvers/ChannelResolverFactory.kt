package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.channels.AbstractChannelResolverFactory
import io.github.freya022.botcommands.internal.parameters.resolvers.channels.IChannelResolver
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@ResolverFactory
@ServiceName("componentChannelResolverFactory")
internal class ChannelResolverFactory(override val context: BContext) : AbstractChannelResolverFactory() {
    internal class ChannelResolver(
        context: BContext,
        private val type: Class<out GuildChannel>,
        override val channelTypes: Set<ChannelType>
    ) : AbstractChannelResolver<ChannelResolver>(context),
        ComponentParameterResolver<ChannelResolver, GuildChannel>,
        // Cannot implement TimeoutParameterResolver
        // as retrieving a channel requires a JDA instance.
        // When a component expired while the bot was offline,
        // the required JDA instance isn't there yet.
        IChannelResolver {

        override suspend fun resolveSuspend(option: ComponentOption, event: GenericComponentInteractionCreateEvent, data: SerializedComponentData): GuildChannel? {
            val guild = event.guild ?: throwArgument("Cannot resolve a channel outside of a guild")
            val channelId = data.asString().toLong()
            val channel = guild.getChannelById(type, channelId)
            if (channel == null) {
                if (ThreadChannel::class.java.isAssignableFrom(type))
                    return retrieveThreadChannel(event, guild, channelId)

                logger.trace { "Could not find channel of type ${type.simpleNestedName} and id $channelId" }
                event.reply(messagesFactory.get(event).resolverChannelNotFound(event, channelId)).setEphemeral(true).queue()
            }

            return channel
        }

        override fun serialize(obj: GuildChannel) = SerializedComponentData.fromString(obj.id)

        private suspend fun retrieveThreadChannel(
            event: GenericComponentInteractionCreateEvent,
            guild: Guild,
            channelId: Long
        ): ThreadChannel? = retrieveThreadChannel(guild, channelId, onMissingAccess = {
            event.reply(messagesFactory.get(event).resolverChannelMissingAccess(event, channelId)).queue()
        })
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

package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.channels.AbstractChannelResolverFactory
import io.github.freya022.botcommands.internal.utils.ifNullThrowInternal
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@ResolverFactory
@ServiceName("textCommandChannelResolverFactory")
@RequiresTextCommands
internal class ChannelResolverFactory(private val messagesFactory: TextCommandsMessagesFactory) : AbstractChannelResolverFactory() {
    internal class ChannelResolver(
        private val messagesFactory: TextCommandsMessagesFactory,
        private val type: Class<out GuildChannel>,
    ) : AbstractChannelResolver<ChannelResolver>(),
        TextParameterResolver<ChannelResolver, GuildChannel> {

        override val pattern: Pattern = channelPattern
        override val testExample: String = "<#1234>"

        override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String =
            event.channel.asMention

        override suspend fun resolveSuspend(
            option: TextCommandOption,
            event: MessageReceivedEvent,
            args: Array<String?>
        ): GuildChannel? {
            val channelId = args.filterNotNull()
                .singleOrNull().ifNullThrowInternal { "Pattern matched but no args were present" }
                .toLongOrNull().ifNullThrowInternal { "ID matched but was not a Long" }
            val channel = event.guild.getChannelById(type, channelId)
            if (channel == null) {
                if (ThreadChannel::class.java.isAssignableFrom(type))
                    return retrieveThreadChannel(event, channelId)
                logger.trace { "Could not find channel of type ${type.simpleNestedName} and id $channelId" }
            }
            return channel
        }

        private suspend fun retrieveThreadChannel(
            event: MessageReceivedEvent,
            channelId: Long
        ): ThreadChannel? = retrieveThreadChannel(event.guild, channelId, onMissingAccess = {
            if (event.channel.canTalk()) {
                event.message.reply(messagesFactory.get(event.message).resolverChannelMissingAccess(event, channelId)).queue()
            }
        })

        private companion object {
            private val channelPattern = Pattern.compile("<#(\\d+)>|(\\d+)")
        }
    }

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> = listOf(TextParameterResolver::class.java)

    override fun isResolvable(request: ResolverRequest): Boolean {
        val parameter = request.parameter
        val erasure = parameter.javaErasure
        return erasure.isSubclassOf<GuildChannel>()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(request: ResolverRequest): IParameterResolver<*> {
        val parameter = request.parameter
        val erasure = parameter.javaErasure as Class<out GuildChannel>
        return ChannelResolver(messagesFactory, erasure)
    }
}

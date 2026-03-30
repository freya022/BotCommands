package io.github.freya022.botcommands.internal.parameters.resolvers.channels

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.utils.ifNullThrowInternal
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@ResolverFactory
internal class ChannelResolverFactory(override val context: BContext) : AbstractChannelResolverFactory() {
    internal class ChannelResolver(
        context: BContext,
        private val type: Class<out GuildChannel>,
        override val channelTypes: Set<ChannelType>
    ) : AbstractChannelResolver<ChannelResolver>(context),
        TextParameterResolver<ChannelResolver, GuildChannel>,
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        IChannelResolver {

        //region Text
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
        //endregion

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

        private suspend fun retrieveThreadChannel(
            event: MessageReceivedEvent,
            channelId: Long
        ): ThreadChannel? = retrieveThreadChannel(event.guild, channelId, onMissingAccess = {
            if (event.channel.canTalk())
                event.message.reply(messagesFactory.get(event).resolverChannelMissingAccess(event, channelId)).queue()
        })

        private companion object {
            private val channelPattern = Pattern.compile("<#(\\d+)>|(\\d+)")
        }
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

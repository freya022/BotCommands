package io.github.freya022.botcommands.internal.parameters.resolvers

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import io.github.freya022.botcommands.internal.components.ComponentDescriptor
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.ChannelType.UNKNOWN
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf

interface IChannelResolver {
    val channelTypes: EnumSet<ChannelType>
}

@ResolverFactory
internal class ChannelResolverFactory(private val context: BContext) : ParameterResolverFactory<ChannelResolverFactory.LimitedChannelResolver>(LimitedChannelResolver::class) {
    // Only for slash commands where Discord always provides the data
    // Channels such as threads are not easily resolvable when used in text commands / components,
    // let the user use the channel id + thread id to find threads themselves
    internal open class LimitedChannelResolver(
        protected val type: Class<out GuildChannel>,
        override val channelTypes: EnumSet<ChannelType>
    ) : ClassParameterResolver<ChannelResolver, GuildChannel>(GuildChannel::class),
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        IChannelResolver {

        //region Slash
        override val optionType: OptionType = OptionType.CHANNEL

        override suspend fun resolveSuspend(
            info: SlashCommandInfo,
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

    internal class ChannelResolver(private val context: BContext, type: Class<out GuildChannel>, channelTypes: EnumSet<ChannelType>) :
        LimitedChannelResolver(type, channelTypes),
        TextParameterResolver<ChannelResolver, GuildChannel>,
        ComponentParameterResolver<ChannelResolver, GuildChannel> {

        //region Text
        override val pattern: Pattern = channelPattern
        override val testExample: String = "<#1234>"

        override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String =
            event.channel.asMention

        override suspend fun resolveSuspend(
            variation: TextCommandVariation,
            event: MessageReceivedEvent,
            args: Array<String?>
        ): GuildChannel? = event.guild.getChannelById(type, args[0]!!)
        //endregion

        //region Component
        override suspend fun resolveSuspend(
            descriptor: ComponentDescriptor,
            event: GenericComponentInteractionCreateEvent,
            arg: String
        ): GuildChannel? {
            val guild = event.guild ?: throwInternal(descriptor.function, "Cannot resolve a Channel outside of a Guild")
            val channel = guild.getChannelById(type, arg)
            if (channel == null) {
                logger.trace { "Could not find channel of type ${type.simpleNestedName} and id $arg" }
                event.reply_(context.getDefaultMessages(event).resolverChannelNotFoundMsg, ephemeral = true).queue()
            }

            return channel
        }
        //endregion

        companion object {
            private val channelPattern = Pattern.compile("(?:<#)?(\\d+)>?")
            private val logger = KotlinLogging.logger { }
        }
    }

    override val supportedTypesStr: List<String> = listOf("<out GuildChannel>")

    @Suppress("UNCHECKED_CAST")
    override fun isResolvable(parameter: ParameterWrapper): Boolean {
        val erasure = parameter.erasure
        if (!erasure.isSubclassOf(GuildChannel::class)) return false
        erasure as KClass<out GuildChannel>

        // Only empty if the type is a GuildChannel but is not a concrete interface
        return erasure == GuildChannel::class || channelTypesFrom(erasure.java).isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(parameter: ParameterWrapper): LimitedChannelResolver {
        val erasure = parameter.erasure as KClass<out GuildChannel>
        val channelTypes = channelTypesFrom(erasure.java)
        if (channelTypes.any { it.isThread }) {
            return LimitedChannelResolver(erasure.java, channelTypes)
        }

        return ChannelResolver(context, erasure.java, channelTypes)
    }
}

private fun channelTypesFrom(clazz: Class<out Channel>): EnumSet<ChannelType> {
    return ChannelType.entries.filterTo(enumSetOf<ChannelType>()) { type -> type.getInterface() == clazz && type != UNKNOWN }
}
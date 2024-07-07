package io.github.freya022.botcommands.internal.parameters.resolvers

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.checkGuildOnly
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.exceptions.InvalidChannelTypeException
import io.github.freya022.botcommands.api.core.reflect.findAnnotation
import io.github.freya022.botcommands.api.core.reflect.function
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ChannelResolverFactory.ChannelResolver
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass

internal sealed interface IChannelResolver {
    val channelTypes: EnumSet<ChannelType>
}

@ResolverFactory
internal class ChannelResolverFactory(private val context: BContext) : ParameterResolverFactory<ChannelResolver>(ChannelResolver::class) {
    internal class ChannelResolver(
        context: BContext,
        private val type: Class<out GuildChannel>,
        override val channelTypes: EnumSet<ChannelType>
    ) : ClassParameterResolver<ChannelResolver, GuildChannel>(GuildChannel::class),
        TextParameterResolver<ChannelResolver, GuildChannel>,
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        ComponentParameterResolver<ChannelResolver, GuildChannel>,
        // Cannot implement TimeoutParameterResolver
        // as retrieving a channel requires a JDA instance.
        // When a component expired while the bot was offline,
        // the required JDA instance isn't there yet.
        IChannelResolver {
            
        private val defaultMessagesFactory: DefaultMessagesFactory = context.getService()

        //region Text
        override val pattern: Pattern = channelPattern
        override val testExample: String = "<#1234>"

        override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String =
            event.channel.asMention

        override suspend fun resolveSuspend(
            variation: TextCommandVariation,
            event: MessageReceivedEvent,
            args: Array<String?>
        ): GuildChannel? {
            val channelId = args[0]!!.toLong()
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

        //region Component
        override suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String): GuildChannel? {
            val guild = event.guild ?: throwArgument("Cannot resolve a channel outside of a guild")
            val channelId = arg.toLong()
            val channel = guild.getChannelById(type, channelId)
            if (channel == null) {
                if (ThreadChannel::class.java.isAssignableFrom(type))
                    return retrieveThreadChannel(event, guild, channelId)

                logger.trace { "Could not find channel of type ${type.simpleNestedName} and id $channelId" }
                event.reply_(defaultMessagesFactory.get(event).resolverChannelNotFoundMsg, ephemeral = true).queue()
            }

            return channel
        }
        //endregion

        private suspend fun retrieveThreadChannel(
            event: MessageReceivedEvent,
            channelId: Long
        ): ThreadChannel? = retrieveThreadChannel(event.guild, channelId, onMissingAccess = {
            if (event.channel.canTalk())
                event.message.reply(defaultMessagesFactory.get(event).getResolverChannelMissingAccessMsg("<#$channelId>")).queue()
        })

        private suspend fun retrieveThreadChannel(
            event: IReplyCallback,
            guild: Guild,
            channelId: Long
        ): ThreadChannel? = retrieveThreadChannel(guild, channelId, onMissingAccess = {
            event.reply_(defaultMessagesFactory.get(event).getResolverChannelMissingAccessMsg("<#$channelId>"), ephemeral = true).queue()
        })

        private suspend fun retrieveThreadChannel(
            guild: Guild,
            channelId: Long,
            onMissingAccess: () -> Unit
        ): ThreadChannel? {
            return guild.retrieveThreadChannelById(channelId).awaitCatching()
                .onErrorResponse(ErrorResponse.UNKNOWN_CHANNEL) {
                    logger.trace { "Could not find thread channel $channelId" }
                    return null
                }
                .onErrorResponse(ErrorResponse.MISSING_ACCESS) {
                    logger.trace { "Could not retrieve thread channel $channelId due to missing access" }
                    onMissingAccess()
                    return null
                }
                .onFailure {
                    if (it is InvalidChannelTypeException) {
                        logger.trace { "Could not retrieve thread channel $channelId is not a thread channel" }
                        return null
                    }
                }
                .getOrThrow()
        }

        private companion object {
            private val channelPattern = Pattern.compile("(?:<#)?(\\d+)>?")
            private val logger = KotlinLogging.logger { }
        }
    }

    override val supportedTypesStr: List<String> = listOf("<out GuildChannel>")

    @Suppress("UNCHECKED_CAST")
    override fun isResolvable(request: ResolverRequest): Boolean {
        val parameter = request.parameter
        val erasure = parameter.erasure
        if (!erasure.isSubclassOf<GuildChannel>()) return false
        erasure as KClass<out GuildChannel>

        request.checkGuildOnly(erasure)

        val channelTypes = when (val annotation = parameter.findAnnotation<ChannelTypes>()) {
            null -> channelTypesFrom(erasure.java)
            else -> enumSetOf(*annotation.value)
        }

        channelTypes.forEach { channelType ->
            require(erasure.isAssignableFrom(channelType.`interface`)) {
                val paramName = parameter.name
                val signature = parameter.function.getSignature(parameterNames = listOf(paramName))
                if (channelTypes.size == 1) {
                    val requireType = channelType.`interface`.simpleName
                    "Channel type was $channelType, meaning that the parameter '$paramName' must use a type that is itself or extends superclasses of $requireType: $signature"
                } else {
                    val compatibleTypes = channelTypes.map { it.`interface` }
                        .map { it.allSuperclassesAndInterfaces.filterTo(linkedSetOf(), GuildChannel::class.java::isAssignableFrom) }
                        .reduce { acc, interfaces ->
                            acc.retainAll(interfaces)
                            acc
                        }
                        .map { it.simpleName }
                    "Channel types were $channelTypes, meaning that the parameter '$paramName' must use a common type such as $compatibleTypes: $signature"
                }
            }
        }

        //TODO future versions of JDA may have a way to disable channel caches (types would be configurable)

        // Only empty if the type is a GuildChannel but is not a concrete interface
        return erasure == GuildChannel::class || channelTypes.isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(request: ResolverRequest): ChannelResolver {
        val parameter = request.parameter
        val erasure = parameter.erasure as KClass<out GuildChannel>
        val channelTypes = when (val annotation = parameter.findAnnotation<ChannelTypes>()) {
            null -> channelTypesFrom(erasure.java)
            else -> enumSetOf(*annotation.value)
        }
        return ChannelResolver(context, erasure.java, channelTypes)
    }

    private fun channelTypesFrom(clazz: Class<out GuildChannel>): EnumSet<ChannelType> {
        return ChannelType.entries.filterTo(enumSetOf<ChannelType>()) { type -> clazz.isAssignableFrom(type.getInterface()) }
    }
}
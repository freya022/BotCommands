package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.function
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import io.github.freya022.botcommands.internal.parameters.resolvers.channels.AbstractChannelResolverFactory
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

@ResolverFactory
internal class ChannelResolverFactory(private val context: BContext) : AbstractChannelResolverFactory() {

    internal class ChannelResolver(
        context: BContext,
        private val type: Class<out GuildChannel>,
        override val channelTypes: Set<ChannelType>
    ) : AbstractChannelResolver<ChannelResolver>(context),
        SlashParameterResolver<ChannelResolver, GuildChannel>,
        IChannelResolver {

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
    }

    override val supportedResolvers: List<Class<out IParameterResolver<*>>> = listOf(SlashParameterResolver::class.java)

    @Suppress("UNCHECKED_CAST")
    override fun isResolvable(request: ResolverRequest): Boolean {
        val parameter = request.parameter
        val erasure = parameter.javaErasure
        if (!erasure.isSubclassOf<GuildChannel>()) return false
        erasure as Class<out GuildChannel>

        request.checkGuildOnly(erasure.kotlin)

        val channelTypes = parameter.getChannelTypes(erasure)
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
        return erasure == GuildChannel::class.java || channelTypes.isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(request: ResolverRequest): IParameterResolver<*> {
        val parameter = request.parameter
        val erasure = parameter.javaErasure as Class<out GuildChannel>
        val channelTypes = parameter.getChannelTypes(erasure)
        return ChannelResolver(context, erasure, channelTypes)
    }

    private fun ParameterWrapper.getChannelTypes(erasure: Class<out GuildChannel>): EnumSet<ChannelType> {
        return parameter.findAllAnnotations<ChannelTypes>()
            .flatMapTo(enumSetOf()) { it.value }
            .ifEmpty { channelTypesFrom(erasure) }
    }

    private fun channelTypesFrom(clazz: Class<out GuildChannel>): EnumSet<ChannelType> {
        return ChannelType.entries.filterTo(enumSetOf<ChannelType>()) { type -> clazz.isAssignableFrom(type.getInterface()) }
    }
}

package io.github.freya022.botcommands.internal.parameters.resolvers.channels

import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.onErrorResponse
import dev.freya02.botcommands.jda.ktx.retrieve.retrieveThreadChannelById
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.exceptions.InvalidChannelTypeException
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.function
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.allSuperclassesAndInterfaces
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.flatMapTo
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.commands.application.checkGuildOnly
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.EnumSet

abstract class AbstractChannelResolverFactory : ParameterResolverFactory() {
    protected abstract val context: BContext

    final override val supportedTypesStr: List<String> = listOf("<out GuildChannel>")

    final override val supportedResolvers = inferSupportedResolversFrom(getResolverType())

    @Suppress("UNCHECKED_CAST")
    final override fun isResolvable(request: ResolverRequest): Boolean {
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
    final override fun get(request: ResolverRequest): IParameterResolver<*> {
        val parameter = request.parameter
        val erasure = parameter.javaErasure as Class<out GuildChannel>
        val channelTypes = parameter.getChannelTypes(erasure)
        return createResolver(context, erasure, channelTypes)
    }

    protected abstract fun getResolverType(): Class<out IParameterResolver<*>>

    protected abstract fun createResolver(context: BContext, erasure: Class<out GuildChannel>, channelTypes: Set<ChannelType>): IParameterResolver<*>

    private fun ParameterWrapper.getChannelTypes(erasure: Class<out GuildChannel>): EnumSet<ChannelType> {
        return parameter.findAllAnnotations<ChannelTypes>()
            .flatMapTo(enumSetOf()) { it.value }
            .ifEmpty { channelTypesFrom(erasure) }
    }

    private fun channelTypesFrom(clazz: Class<out GuildChannel>): EnumSet<ChannelType> {
        return ChannelType.entries.filterTo(enumSetOf<ChannelType>()) { type -> clazz.isAssignableFrom(type.getInterface()) }
    }

    abstract class AbstractChannelResolver<T : AbstractChannelResolver<T>>(
        protected val context: BContext
    ) : ClassParameterResolver<T, GuildChannel>(GuildChannel::class) {
        protected val logger = objectLogger()

        protected val messagesFactory: BotCommandsMessagesFactory = context.getService()

        protected suspend fun retrieveThreadChannel(
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
    }
}

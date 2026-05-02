package io.github.freya022.botcommands.internal.parameters.resolvers.channels

import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.onErrorResponse
import dev.freya02.botcommands.jda.ktx.retrieve.retrieveThreadChannelById
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.exceptions.InvalidChannelTypeException
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.ErrorResponse

abstract class AbstractChannelResolverFactory : ParameterResolverFactory() {
    final override val supportedTypesStr: List<String> = listOf("<out GuildChannel>")

    abstract class AbstractChannelResolver<T : AbstractChannelResolver<T>> : ClassParameterResolver<T, GuildChannel>(GuildChannel::class) {
        protected val logger = objectLogger()

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

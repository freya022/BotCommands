package io.github.freya022.botcommands.internal.commands.text.ratelimit

import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.internal.ratelimit.DefaultBucketKeySupplier
import io.github.freya022.botcommands.internal.utils.throwInternal

internal class DefaultBucketKeySupplierRequestHandler : DefaultBucketKeySupplier.RequestHandler {

    override fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): BucketKeySupplier.Key? {
        if (context is TextCommandRateLimitingContext) {
            val event = context.event
            val commandInfo = context.commandInfo

            if (!event.isFromGuild) throwInternal("Text commands can't run outside of a guild")
            val path = commandInfo.path
            return when (instance.scope) {
                RateLimitScope.USER -> BucketKeySupplier.UserKey(path.fullPath, event.author.idLong)
                RateLimitScope.USER_PER_GUILD -> BucketKeySupplier.UserAtPlaceKey(
                    path.fullPath,
                    event.guild.idLong,
                    event.author.idLong
                )
                RateLimitScope.USER_PER_CHANNEL -> BucketKeySupplier.UserAtPlaceKey(
                    path.fullPath,
                    event.channel.idLong,
                    event.author.idLong
                )
                RateLimitScope.GUILD -> BucketKeySupplier.PlaceKey(path.fullPath, event.guild.idLong)
                RateLimitScope.CHANNEL -> BucketKeySupplier.PlaceKey(path.fullPath, event.channel.idLong)
            }
        }

        return null
    }
}

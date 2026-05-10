package io.github.freya022.botcommands.internal.commands.application.ratelimit

import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.internal.ratelimit.DefaultBucketKeySupplier
import net.dv8tion.jda.api.interactions.commands.CommandInteraction

internal class DefaultBucketKeySupplierRequestHandler : DefaultBucketKeySupplier.RequestHandler {

    override fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): BucketKeySupplier.Key? {
        if (context is ApplicationCommandRateLimitingContext) {
            return instance.getRateLimitKey(context.event, context.event.uniqueCommandPath)
        }

        return null
    }

    private val CommandInteraction.uniqueCommandPath: String
        get() = buildString(/* 19 + 8 (average length) */ 27) {
            append(commandIdLong)
            if (subcommandGroup != null)
                append(' ').append(subcommandGroup)
            if (subcommandName != null)
                append(' ').append(subcommandName)
        }
}

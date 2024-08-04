package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory

internal class RateLimitBuilderImpl internal constructor(
    override val group: String,
    override var bucketFactory: BucketFactory,
    override var limiterFactory: RateLimiterFactory
) : RateLimitBuilder {
    override lateinit var declarationSite: DeclarationSite

    internal fun build(): RateLimitInfo {
        return RateLimitInfo(group, limiterFactory.get(bucketFactory), declarationSite)
    }
}
package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.commands.ratelimit.RateLimitHelper
import com.freya02.botcommands.api.commands.ratelimit.RateLimitHelperFactory
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.core.utils.enumSetOf
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.CommandDSL
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import com.freya02.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.Permission
import java.util.*

@CommandDSL
abstract class CommandBuilder internal constructor(protected val context: BContextImpl, override val name: String) : INamedCommand {
    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    final override val path: CommandPath by lazy { computePath() }

    internal var rateLimitInfo: RateLimitInfo? = null
        private set

    fun rateLimit(
        bucketFactory: BucketFactory,
        helperFactory: RateLimitHelperFactory = RateLimitHelper.defaultFactory(RateLimitScope.USER),
        group: String = path.fullPath,
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer { }
    ) {
        rateLimitInfo = context.getService<RateLimitContainer>().rateLimit(group, bucketFactory, helperFactory, block)
    }

    fun rateLimitReference(group: String) {
        rateLimitInfo = context.getService<RateLimitContainer>()[group]
            ?: throwUser("Could not find a rate limiter for '$group'")
    }
}

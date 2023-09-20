package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo

internal sealed interface RateLimited {
    val path: CommandPath
    val rateLimitInfo: RateLimitInfo?
}
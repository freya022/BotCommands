package io.github.freya022.botcommands.api.ratelimit

import io.github.freya022.botcommands.api.core.BContext

// TODO move to ratelimit package (outside of commands)
interface RateLimitingContext {
    val context: BContext
}

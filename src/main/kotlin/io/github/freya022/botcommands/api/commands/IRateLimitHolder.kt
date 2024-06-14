package io.github.freya022.botcommands.api.commands

interface IRateLimitHolder {
    fun hasRateLimiter(): Boolean
}
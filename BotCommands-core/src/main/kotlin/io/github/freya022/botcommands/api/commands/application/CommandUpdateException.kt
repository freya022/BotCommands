package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.internal.utils.shortSignature
import kotlin.reflect.KFunction

/**
 * Simple wrapper for the failing command declaration function and its exception
 *
 * @see CommandUpdateResult
 */
class CommandUpdateException internal constructor(val function: KFunction<Unit>, val throwable: Throwable) {
    override fun toString(): String = "$throwable at: ${function.shortSignature}"
}

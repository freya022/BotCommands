package dev.freya02.botcommands.jda.ktx.components.utils

import dev.freya02.botcommands.jda.ktx.components.exceptions.ComponentConfigurationException
import net.dv8tion.jda.api.components.Component

@Suppress("UnusedReceiverParameter", "unused")
@Deprecated(level = DeprecationLevel.ERROR, message = "Check nullable property instead")
internal fun Any.checkInit(name: String): Nothing {
    error("Unreachable")
}

internal fun <T : Any> T?.checkInit(name: String): T {
    if (this == null) throw ComponentConfigurationException("$name is not initialized")
    return this
}

@Suppress("UnusedReceiverParameter")
internal fun Component.checkInit() { // do not inline
    // Nothing, the property this function is called on should do the proper init check
}

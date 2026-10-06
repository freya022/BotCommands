package dev.freya02.botcommands.jda.ktx.components.exceptions

/**
 * Exception thrown when attempting to build a misconfigured component.
 */
class ComponentConfigurationException internal constructor(message: String) : IllegalStateException(message)

package io.github.freya022.botcommands.internal.core.service.exceptions

internal class ServiceCreationException internal constructor(
    cause: Throwable,
    override val message: String,
) : RuntimeException(cause)
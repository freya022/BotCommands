package io.github.freya022.botcommands.internal.parameters.resolvers.exceptions

internal class MissingResolverSuperclass internal constructor(
    message: String
) : IllegalStateException(message)
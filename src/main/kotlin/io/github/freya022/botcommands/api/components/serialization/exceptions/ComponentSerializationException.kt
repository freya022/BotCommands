package io.github.freya022.botcommands.api.components.serialization.exceptions

import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver

/**
 * An exception thrown when [ComponentParameterResolver.serialize] fails.
 */
class ComponentSerializationException : RuntimeException {

    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
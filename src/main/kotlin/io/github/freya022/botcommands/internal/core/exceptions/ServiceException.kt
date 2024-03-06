package io.github.freya022.botcommands.internal.core.exceptions

import io.github.freya022.botcommands.api.core.service.ServiceError

internal class ServiceException internal constructor(
    val serviceError: ServiceError
) : RuntimeException("\n${serviceError.toDetailedString()}")
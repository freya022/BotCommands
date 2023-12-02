package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

// Must not be implemented
@InterfacedService(acceptMultiple = false)
internal sealed interface UnusedInterfacedService
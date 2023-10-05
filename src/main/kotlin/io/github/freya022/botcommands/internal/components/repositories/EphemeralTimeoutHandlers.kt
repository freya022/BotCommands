package io.github.freya022.botcommands.internal.components.repositories

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

@BService
@Dependencies(Components::class)
internal class EphemeralTimeoutHandlers : EphemeralHandlers<suspend () -> Unit>()
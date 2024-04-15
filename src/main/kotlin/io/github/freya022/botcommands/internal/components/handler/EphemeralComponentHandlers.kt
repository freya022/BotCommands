package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.components.EphemeralHandlers

@BService
@Dependencies(Components::class)
@RequiresComponents
internal class EphemeralComponentHandlers : EphemeralHandlers<EphemeralHandler<*>>()
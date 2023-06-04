package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.internal.components.EphemeralHandler

@BService
@Dependencies(Components::class)
internal class EphemeralComponentHandlers : EphemeralHandlers<EphemeralHandler<*>>()
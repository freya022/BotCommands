package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.internal.components.EphemeralHandler

@BService
@ConditionalService(dependencies = [Components::class])
internal class EphemeralComponentHandlers : EphemeralHandlers<EphemeralHandler<*>>()
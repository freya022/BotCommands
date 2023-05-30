package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.Dependencies

@BService
@Dependencies([Components::class])
internal class EphemeralTimeoutHandlers : EphemeralHandlers<suspend () -> Unit>()
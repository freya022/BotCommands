package com.freya02.botcommands.internal.components.repositories

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.components.EphemeralHandler

@BService
internal class EphemeralComponentHandlers : EphemeralHandlers<EphemeralHandler<*>>()
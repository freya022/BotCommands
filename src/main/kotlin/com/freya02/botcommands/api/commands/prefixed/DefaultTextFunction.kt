package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.internal.throwInternal

@LateService
class DefaultTextFunction {
    fun guild(event: BaseCommandEvent): Nothing = throwInternal("Default function was used")
}

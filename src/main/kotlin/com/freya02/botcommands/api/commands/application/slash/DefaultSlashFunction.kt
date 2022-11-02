package com.freya02.botcommands.api.commands.application.slash

import com.freya02.botcommands.api.core.annotations.LateService
import com.freya02.botcommands.internal.throwInternal

@LateService
class DefaultSlashFunction {
    fun global(event: GlobalSlashEvent): Nothing = throwInternal("Default function was used")
    fun guild(event: GuildSlashEvent): Nothing = throwInternal("Default function was used")
}
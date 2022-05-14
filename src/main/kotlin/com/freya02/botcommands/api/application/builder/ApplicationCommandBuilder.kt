package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.builder.CommandBuilder

abstract class ApplicationCommandBuilder internal constructor(path: CommandPath) : CommandBuilder(path) {
    var scope: CommandScope = CommandScope.GUILD
    var defaultLocked = false
    var guildOnly = false
    var testOnly = false
}

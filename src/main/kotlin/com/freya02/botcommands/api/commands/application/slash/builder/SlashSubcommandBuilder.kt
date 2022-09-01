package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo

class SlashSubcommandBuilder internal constructor(
    context: BContextImpl,
    name: String
) : SlashCommandBuilder(context, name) {
    internal fun build(): SlashCommandInfo {
        checkFunction()
        return SlashSubcommandInfo(context, this)
    }
}

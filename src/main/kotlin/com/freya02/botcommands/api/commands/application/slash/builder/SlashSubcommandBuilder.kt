package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashSubcommandInfo
import com.freya02.botcommands.internal.commands.application.slash.TopLevelSlashCommandInfo

class SlashSubcommandBuilder internal constructor(
    context: BContextImpl,
    name: String
) : SlashCommandBuilder(context, name) {
    internal fun build(topLevelInstance: TopLevelSlashCommandInfo): SlashCommandInfo {
        checkFunction()
        return SlashSubcommandInfo(context, topLevelInstance, this)
    }
}

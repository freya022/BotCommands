package com.freya02.botcommands.internal.commands.application.mixins

import com.freya02.botcommands.api.commands.CommandPath

interface INamedCommandInfo {
    val parentInstance: INamedCommandInfo?

    val name: String
    val _path: CommandPath
}
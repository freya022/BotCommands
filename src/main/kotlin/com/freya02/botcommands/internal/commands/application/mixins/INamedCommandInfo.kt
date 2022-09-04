package com.freya02.botcommands.internal.commands.application.mixins

interface INamedCommandInfo {
    val parentInstance: INamedCommandInfo

    val name: String
}
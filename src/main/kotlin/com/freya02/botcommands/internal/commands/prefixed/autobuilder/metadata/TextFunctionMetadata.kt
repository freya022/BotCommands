package com.freya02.botcommands.internal.commands.prefixed.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import com.freya02.botcommands.internal.core.ClassPathFunction

internal class TextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDATextCommand,
    path: CommandPath
) : CommandFunctionMetadata<TextCommand, JDATextCommand>(classPathFunction, TextCommand::class, annotation, path)
package io.github.freya022.botcommands.internal.commands.prefixed.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal class TextFunctionMetadata(
    classPathFunction: ClassPathFunction,
    annotation: JDATextCommand,
    path: CommandPath
) : CommandFunctionMetadata<TextCommand, JDATextCommand>(classPathFunction, TextCommand::class, annotation, path)
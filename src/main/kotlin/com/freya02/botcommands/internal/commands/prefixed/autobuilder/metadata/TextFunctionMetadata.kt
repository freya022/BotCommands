package com.freya02.botcommands.internal.commands.prefixed.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import kotlin.reflect.KFunction

internal class TextFunctionMetadata(
    instance: TextCommand,
    func: KFunction<*>,
    annotation: JDATextCommand,
    path: CommandPath
) : CommandFunctionMetadata<TextCommand, JDATextCommand>(instance, func, annotation, path)
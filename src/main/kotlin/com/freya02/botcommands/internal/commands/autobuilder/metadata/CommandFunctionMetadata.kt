package com.freya02.botcommands.internal.commands.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import kotlin.reflect.KFunction

internal abstract class CommandFunctionMetadata<T, A : Annotation>(
    val instance: T,
    val func: KFunction<*>,
    val annotation: A,
    val path: CommandPath
)
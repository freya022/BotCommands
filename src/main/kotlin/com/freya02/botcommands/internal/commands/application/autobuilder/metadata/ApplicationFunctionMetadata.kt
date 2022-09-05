package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import kotlin.reflect.KFunction

internal abstract class ApplicationFunctionMetadata<T, A : Annotation>(
    instance: T,
    func: KFunction<*>,
    annotation: A,
    path: CommandPath,
    val commandId: String?
) : CommandFunctionMetadata<T, A>(instance, func, annotation, path)
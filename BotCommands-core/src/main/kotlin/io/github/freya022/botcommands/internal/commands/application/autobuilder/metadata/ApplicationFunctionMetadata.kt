package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal abstract class ApplicationFunctionMetadata<A : Annotation>(
    classPathFunction: ClassPathFunction,
    annotation: A,
    path: CommandPath,
    val commandId: String?
) : CommandFunctionMetadata<A>(classPathFunction, annotation, path)

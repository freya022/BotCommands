package com.freya02.botcommands.internal.commands.application.autobuilder.metadata

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import com.freya02.botcommands.internal.core.ClassPathFunction

internal abstract class ApplicationFunctionMetadata<A : Annotation>(
    classPathFunction: ClassPathFunction,
    annotation: A,
    path: CommandPath,
    val commandId: String?
) : CommandFunctionMetadata<ApplicationCommand, A>(classPathFunction, ApplicationCommand::class, annotation, path)
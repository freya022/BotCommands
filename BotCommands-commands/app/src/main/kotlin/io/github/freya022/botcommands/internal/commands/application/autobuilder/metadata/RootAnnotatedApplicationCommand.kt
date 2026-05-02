package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder

internal interface RootAnnotatedApplicationCommand : MetadataFunctionHolder {
    val metadata: ApplicationFunctionMetadata<*>
    val scope: CommandScope
    val name: String
}

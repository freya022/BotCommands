package io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import kotlin.reflect.KFunction

internal sealed interface SlashCommandMetadata : RootAnnotatedApplicationCommand {
    val annotation: TopLevelSlashCommandData
    override val metadata: SlashFunctionMetadata

    override val scope: CommandScope
        get() = annotation.scope
    override val name: String
        get() = metadata.path.name

    class TopLevel(
        override val annotation: TopLevelSlashCommandData,
        override val metadata: SlashFunctionMetadata,
    ) : SlashCommandMetadata {
        override val func: KFunction<*> get() = metadata.func
    }

    class Grouped private constructor(
        override val annotation: TopLevelSlashCommandData,
        override val metadata: SlashFunctionMetadata,
        val subcommands: List<SlashFunctionMetadata>,
        val subcommandGroups: Map<String, SubcommandGroup>,
    ) : SlashCommandMetadata {
        override val func: KFunction<*> get() = metadata.func

        internal class Builder(
            val name: String,
            val annotation: TopLevelSlashCommandData,
            val metadata: SlashFunctionMetadata,
        ) {
            val subcommands: MutableList<SlashFunctionMetadata> = arrayListOf()
            val subcommandGroups: MutableMap<String, SubcommandGroup.Builder> = hashMapOf()

            fun build() = Grouped(annotation, metadata, subcommands.toImmutableList(), subcommandGroups.mapValues { (_, builder) -> builder.build() })
        }

        internal class SubcommandGroup private constructor(val name: String, val properties: Properties, val subcommands: List<SlashFunctionMetadata>) {
            inline fun filterSubcommands(block: (SlashFunctionMetadata) -> Boolean): SubcommandGroup {
                return SubcommandGroup(name, properties, subcommands.filter(block))
            }

            internal class Properties(val description: String)

            internal class Builder(val name: String) {
                lateinit var properties: Properties

                val subcommands: MutableList<SlashFunctionMetadata> = arrayListOf()

                fun build() = SubcommandGroup(name, properties, subcommands.toImmutableList())
            }
        }
    }
}

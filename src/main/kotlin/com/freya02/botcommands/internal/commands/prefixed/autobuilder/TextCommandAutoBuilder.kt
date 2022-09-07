package com.freya02.botcommands.internal.commands.prefixed.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.annotations.RequireOwner
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.*
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.autobuilder.fillCommandBuilder
import com.freya02.botcommands.internal.commands.autobuilder.forEachWithDelayedExceptions
import com.freya02.botcommands.internal.commands.autobuilder.nullIfEmpty
import com.freya02.botcommands.internal.commands.prefixed.autobuilder.metadata.TextFunctionMetadata
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@BService
internal class TextCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val functions: List<TextFunctionMetadata>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDATextCommand>()
            .requireNonStatic()
            .requireFirstArg(BaseCommandEvent::class).map {
                val instance = it.instance as? TextCommand
                    ?: throwUser(it.function, "Declaring class must extend ${TextCommand::class.simpleName}")
                val func = it.function
                val annotation = func.findAnnotation<JDATextCommand>() ?: throwInternal("@JDATextCommand should be present")
                val path = CommandPath.of(annotation.name, annotation.group.nullIfEmpty(), annotation.subcommand.nullIfEmpty()).also { path ->
                    if (path.group != null && path.nameCount == 2) {
                        throwUser(func, "Slash commands with groups need to have their subcommand name set")
                    }
                }

                TextFunctionMetadata(instance, func, annotation, path)
            }
    }

    fun declare(manager: TextCommandManager) {
        val subcommands: MutableMap<String, MutableList<TextFunctionMetadata>> = hashMapOf()
        val subcommandGroups: MutableMap<String, TextSubcommandGroupMetadata> = hashMapOf()

        functions.forEachWithDelayedExceptions { metadata ->
            when (metadata.path.nameCount) {
                2 -> subcommands.computeIfAbsent(metadata.path.name) { arrayListOf() }.add(metadata)
                3 -> subcommandGroups
                    .computeIfAbsent(metadata.path.name) {
                        TextSubcommandGroupMetadata(
                            metadata.path.group!!,
                            metadata.annotation.description
                        )
                    }
                    .subcommands
                    .computeIfAbsent(metadata.path.group!!) { arrayListOf() }
                    .add(metadata)
            }
        }

        functions.forEachWithDelayedExceptions {
            processCommand(manager, it, subcommands, subcommandGroups)
        }
    }

    private fun processCommand(
        manager: TextCommandManager,
        metadata: TextFunctionMetadata,
        subcommands: Map<String, MutableList<TextFunctionMetadata>>,
        subcommandGroups: Map<String, TextSubcommandGroupMetadata>
    ) {
        manager.textCommand(metadata.path.name) {
            if (metadata.path.nameCount == 1) {
                processBuilder(metadata, arrayListOf(name))
            }

            subcommands[name]?.let { metadataList ->
                metadataList.forEach { subMetadata ->
                    subcommand(subMetadata.path.subname!!) {
                        processBuilder(subMetadata, arrayListOf(metadata.path.name, subMetadata.path.subname!!))
                    }
                }
            }

            subcommandGroups[name]?.let { groupMetadata ->
                subcommand(groupMetadata.name) {
                    description = groupMetadata.description

                    groupMetadata.subcommands.forEach { (subname, metadataList) ->
                        metadataList.forEach { subMetadata ->
                            subcommand(subname) {
                                processBuilder(subMetadata, arrayListOf(metadata.path.name, groupMetadata.name, subMetadata.path.subname!!))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun TextCommandBuilder.processBuilder(metadata: TextFunctionMetadata, pathComponents: MutableList<String>) {
        val func = metadata.func
        val annotation = metadata.annotation
        val instance = metadata.instance

        fillCommandBuilder(func, true) //TODO fix

        func.findAnnotation<Category>()?.let { category = it.value }
        aliases = annotation.aliases.map { CommandPath.of(it) }.toMutableList()
        description = annotation.description

        order = annotation.order
        hidden = func.hasAnnotation<Hidden>()
        ownerRequired = func.hasAnnotation<RequireOwner>()

        detailedDescription = instance.detailedDescription

        processOptions(func, instance, CommandPath.of(*pathComponents.toTypedArray()))
    }

    private fun TextCommandBuilder.processOptions(func: KFunction<*>, instance: TextCommand, path: CommandPath) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            when (val optionAnnotation = kParameter.findAnnotation<TextOption>()) {
                null -> when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> customOption(kParameter.findDeclarationName())
                    else -> generatedOption(
                        kParameter.findDeclarationName(), instance.getGeneratedValueSupplier(
                            path,
                            kParameter.findOptionName().asDiscordString(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
                else -> option(optionAnnotation.name.nullIfEmpty() ?: kParameter.findDeclarationName()) {
                    helpExample = optionAnnotation.example
                    isId = kParameter.hasAnnotation<ID>()
                }
            }
        }
    }

    private class TextSubcommandGroupMetadata(val name: String, val description: String) {
        val subcommands: MutableMap<String, MutableList<TextFunctionMetadata>> = hashMapOf()
    }
}
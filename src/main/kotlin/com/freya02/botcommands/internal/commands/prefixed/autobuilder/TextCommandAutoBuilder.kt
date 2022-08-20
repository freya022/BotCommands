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
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.autobuilder.fillCommandBuilder
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.findOptionName
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@BService
internal class TextCommandAutoBuilder(classPathContainer: ClassPathContainer) {
    private val functions: List<ClassPathFunction>

    init {
        functions = classPathContainer.functionsWithAnnotation<JDATextCommand>()
            .requireNonStatic()
            .requireFirstArg(BaseCommandEvent::class)
    }

    @TextDeclaration
    fun declare(manager: TextCommandManager) {
        functions.forEach {
            val func = it.function
            val annotation = func.findAnnotation<JDATextCommand>()!!

            processCommand(manager, annotation, func, it)
        }
    }

    private fun processCommand(
        manager: TextCommandManager,
        annotation: JDATextCommand,
        func: KFunction<*>,
        classPathFunction: ClassPathFunction
    ) {
        val instance = classPathFunction.instance as? TextCommand ?: throwUser(
            classPathFunction.function,
            "Declaring class must extend ${TextCommand::class.simpleName}"
        )

        val path = CommandPath.of(annotation.name, annotation.group.nullIfEmpty(), annotation.subcommand.nullIfEmpty())

        manager.textCommand(path) {
            fillCommandBuilder(func)

            func.findAnnotation<Category>()?.let { category = it.value }
            aliases = annotation.aliases.map { CommandPath.of(it) }.toMutableList()
            description = annotation.description

            order = annotation.order
            hidden = func.hasAnnotation<Hidden>()
            ownerRequired = func.hasAnnotation<RequireOwner>()

            detailedDescription = instance.detailedDescription

            processOptions(func, instance)
        }
    }

    private fun String.nullIfEmpty(): String? = when {
        isEmpty() -> null
        else -> this
    }

    private fun TextCommandBuilder.processOptions(
        func: KFunction<*>,
        instance: TextCommand,
    ) {
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
}
package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandsDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GuildApplicationCommandsDeclaration
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.commands.autobuilder.requireCustomOption
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.findOptionName
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

internal sealed class ContextCommandAutoBuilder(
    context: BContextImpl,
    private val resolverContainer: ResolverContainer
) : GlobalApplicationCommandsDeclaration, GuildApplicationCommandsDeclaration {
    protected val forceGuildCommands = context.applicationConfig.forceGuildCommands

    protected fun ApplicationCommandBuilder<*>.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            if (kParameter.hasAnnotation<ContextOption>()) {
                when (this) {
                    is UserCommandBuilder -> option(kParameter.findDeclarationName())
                    is MessageCommandBuilder -> option(kParameter.findDeclarationName())
                }
            } else {
                when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> {
                        resolverContainer.requireCustomOption(func, kParameter, ContextOption::class)
                        customOption(kParameter.findDeclarationName())
                    }
                    else -> generatedOption(
                        kParameter.findDeclarationName(), instance.getGeneratedValueSupplier(
                            guild,
                            commandId,
                            CommandPath.ofName(name),
                            kParameter.findOptionName(),
                            ParameterType.ofType(kParameter.type)
                        )
                    )
                }
            }
        }
    }
}
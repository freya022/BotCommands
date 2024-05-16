package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.autobuilder.requireServiceOptionOrOptional
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.findOptionName
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

internal sealed class ContextCommandAutoBuilder(
    override val serviceContainer: ServiceContainer,
    applicationConfig: BApplicationConfig,
    private val resolverContainer: ResolverContainer
) : CommandAutoBuilder, GlobalApplicationCommandProvider, GuildApplicationCommandProvider {

    override val optionAnnotation: KClass<out Annotation> = ContextOption::class

    protected val forceGuildCommands = applicationConfig.forceGuildCommands

    protected fun ApplicationCommandBuilder<*>.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val declaredName = kParameter.findDeclarationName()
            if (kParameter.hasAnnotation<ContextOption>()) {
                when (this) {
                    is UserCommandBuilder -> option(declaredName)
                    is MessageCommandBuilder -> option(declaredName)
                }
            } else {
                when (kParameter.findAnnotation<GeneratedOption>()) {
                    null -> {
                        if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(kParameter.wrap())) {
                            customOption(declaredName)
                        } else {
                            requireServiceOptionOrOptional(func, kParameter)
                            serviceOption(declaredName)
                        }
                    }
                    else -> generatedOption(
                        declaredName, instance.getGeneratedValueSupplier(
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
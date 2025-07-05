package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationOptionRegistry
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.application.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.autobuilder.requireServiceOptionOrOptional
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal sealed class ContextCommandAutoBuilder(
    override val serviceContainer: ServiceContainer,
    applicationConfig: BApplicationConfig,
    private val resolverContainer: ResolverContainer
) : CommandAutoBuilder, GlobalApplicationCommandProvider, GuildApplicationCommandProvider {

    protected abstract val commandAnnotation: KClass<out Annotation>
    override val optionAnnotation: KClass<out Annotation> = ContextOption::class

    protected val forceGuildCommands = applicationConfig.forceGuildCommands

    protected fun ApplicationCommandBuilder<*>.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        commandId: String?
    ) {
        func.nonInstanceParameters.drop(1).forEach { kParameter ->
            val paramType = kParameter.type.jvmErasure
            if (paramType.isValue) {
                inlineClassAggregate(kParameter.findDeclarationName(), paramType) { valueParameter, _ ->
                    addOption(this@inlineClassAggregate, guild, func, instance, path, commandId, ParameterAdapter(kParameter, valueParameter))
                }
            } else {
                addOption(this@processOptions, guild, func, instance, path, commandId, ParameterAdapter(kParameter, kParameter))
            }
        }
    }

    private fun addOption(
        registry: ApplicationOptionRegistry<*>,
        guild: Guild?,
        func: KFunction<*>,
        instance: ApplicationCommand,
        path: CommandPath,
        commandId: String?,
        parameter: ParameterAdapter
    ) {
        val optionAnnotation = parameter.findAnnotation<ContextOption>()
        if (optionAnnotation != null) {
            when (registry) {
                is UserCommandOptionRegistry -> registry.option(parameter.declaredName)
                is MessageCommandOptionRegistry -> registry.option(parameter.declaredName)
            }
        } else if (parameter.hasAnnotation<GeneratedOption>()) {
            val valueSupplier = instance.getGeneratedValueSupplier(guild, commandId, path, parameter.discordName, parameter.actualType)
            registry.generatedOption(parameter.declaredName, valueSupplier)
        } else if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(parameter.valueParameter.wrap())) {
            registry.customOption(parameter.declaredName)
        } else {
            requireServiceOptionOrOptional(func, parameter, commandAnnotation)
            registry.serviceOption(parameter.declaredName)
        }
    }
}
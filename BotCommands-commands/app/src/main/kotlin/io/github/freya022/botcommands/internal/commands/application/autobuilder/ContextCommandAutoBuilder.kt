package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplierProvider
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationOptionRegistry
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.RootAnnotatedApplicationCommand
import io.github.freya022.botcommands.internal.commands.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.checkAt
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal sealed class ContextCommandAutoBuilder<T : RootAnnotatedApplicationCommand>(
    override val serviceContainer: ServiceContainer,
    applicationConfig: BApplicationConfig,
    private val resolverContainer: ResolverContainer
) : ApplicationCommandAutoBuilder<T>(applicationConfig) {

    protected abstract val commandAnnotation: KClass<out Annotation>
    override val optionAnnotation: KClass<out Annotation> = ContextOption::class

    protected fun ApplicationCommandBuilder<*>.processOptions(
        guild: Guild?,
        func: KFunction<*>,
        instance: Any,
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
        instance: Any,
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
            checkAt(instance is ApplicationGeneratedValueSupplierProvider, func) {
                "Declaring class must extend ${classRef<ApplicationGeneratedValueSupplierProvider>()}"
            }

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

package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationOptionRegistry
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.options.builder.inlineClassAggregate
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.autobuilder.requireServiceOptionOrOptional
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.findOptionName
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
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
            val declaredName = kParameter.findDeclarationName()
            val optionAnnotation = kParameter.findAnnotation<ContextOption>()
            if (optionAnnotation != null) {
                fun ApplicationOptionRegistry<*>.addOption(valueName: String) {
                    when (this) {
                        is UserCommandOptionRegistry -> option(valueName)
                        is MessageCommandOptionRegistry -> option(valueName)
                    }
                }

                if (kParameter.type.jvmErasure.isValue) {
                    val inlineClassType = kParameter.type.jvmErasure
                    inlineClassAggregate(declaredName, inlineClassType) { valueName ->
                        addOption(valueName)
                    }
                } else {
                    addOption(declaredName)
                }
            } else if (kParameter.hasAnnotation<GeneratedOption>()) {
                generatedOption(
                    declaredName, instance.getGeneratedValueSupplier(
                        guild,
                        commandId,
                        CommandPath.ofName(name),
                        kParameter.findOptionName(),
                        ParameterType.ofType(kParameter.type)
                    )
                )
            } else if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(kParameter.wrap())) {
                customOption(declaredName)
            } else {
                requireServiceOptionOrOptional(func, kParameter, commandAnnotation)
                serviceOption(declaredName)
            }
        }
    }
}
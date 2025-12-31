package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.provider.*
import io.github.freya022.botcommands.api.commands.text.annotations.NSFW
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.ApplicationFunctionMetadata
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.RootAnnotatedApplicationCommand
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandAutoBuilder
import io.github.freya022.botcommands.internal.commands.autobuilder.forEachWithDelayedExceptions
import io.github.freya022.botcommands.internal.utils.*
import net.dv8tion.jda.api.interactions.commands.Command as JDACommand
import kotlin.reflect.KFunction

internal abstract class ApplicationCommandAutoBuilder<T : RootAnnotatedApplicationCommand>(
    applicationConfig: BApplicationConfig
) : CommandAutoBuilder(),
    GlobalApplicationCommandProvider,
    GuildApplicationCommandProvider {

    private val logger = this.objectLogger()

    protected val forceGuildCommands: Boolean = applicationConfig.forceGuildCommands
    protected abstract val commandType: JDACommand.Type

    protected abstract val rootAnnotatedCommands: Collection<T>

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        // On global manager, do not register any command if forceGuildCommands is enabled,
        // as none of them would be global
        if (forceGuildCommands)
            return

        val skipLogger = SkipLogger(logger)
        rootAnnotatedCommands.forEachWithDelayedExceptions forEach@{ rootAnnotatedCommand ->
            val scope = rootAnnotatedCommand.scope
            if (scope != CommandScope.GLOBAL) return@forEach

            val testState = checkTestCommand(manager, rootAnnotatedCommand.func, scope, manager.context)
            if (testState != TestState.NO_ANNOTATION)
                throwInternal("Test commands on a global scope should have thrown in ${::checkTestCommand.shortSignatureNoSrc}")

            context(skipLogger) { declareTopLevel(manager, rootAnnotatedCommand) }
        }

        skipLogger.log(guild = null, commandType)
    }

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {
        val skipLogger = SkipLogger(logger)
        rootAnnotatedCommands.forEachWithDelayedExceptions forEach@{ rootAnnotatedCommand ->
            val scope = rootAnnotatedCommand.scope

            // If guild commands aren't forced, check the scope
            val canBeDeclared = forceGuildCommands || scope == CommandScope.GUILD
            if (!canBeDeclared) return@forEach

            val testState = checkTestCommand(manager, rootAnnotatedCommand.func, scope, manager.context)
            if (scope == CommandScope.GLOBAL && testState != TestState.NO_ANNOTATION)
                throwInternal("Test commands on a global scope should have thrown in ${::checkTestCommand.shortSignatureNoSrc}")

            if (testState == TestState.EXCLUDE)
                return@forEach skipLogger.skip(rootAnnotatedCommand.name, "Is a test command while this guild isn't a test guild")

            context(skipLogger) { declareTopLevel(manager, rootAnnotatedCommand) }
        }

        skipLogger.log(manager.guild, commandType)
    }

    private fun checkTestCommand(manager: AbstractApplicationCommandManager, func: KFunction<*>, scope: CommandScope, context: BContext): TestState {
        if (func.hasAnnotationRecursive<Test>()) {
            requireAt(scope == CommandScope.GUILD, func) {
                "Test commands must have their scope set to GUILD"
            }
            if (manager !is GuildApplicationCommandManager) throwInternal("GUILD scoped command was not registered with a guild command manager")

            //Returns whether the command can be registered
            return when (manager.guild.idLong) {
                in AnnotationUtils.getEffectiveTestGuildIds(context, func) -> TestState.INCLUDE
                else -> TestState.EXCLUDE
            }
        }

        return TestState.NO_ANNOTATION
    }

    context(logger: SkipLogger)
    protected abstract fun declareTopLevel(manager: AbstractApplicationCommandManager, rootCommand: T)

    context(logger: SkipLogger)
    internal fun checkDeclarationFilter(
        manager: AbstractApplicationCommandManager,
        metadata: ApplicationFunctionMetadata<*>,
    ): Boolean {
        val func = metadata.func
        val path = metadata.path
        val commandId = metadata.commandId

        func.findAllAnnotations<DeclarationFilter>().forEach { declarationFilter ->
            checkAt(manager is GuildApplicationCommandManager, func) {
                "${annotationRef<DeclarationFilter>()} can only be used on guild commands"
            }

            declarationFilter.filters.forEach {
                if (!serviceContainer.getService(it).filter(manager.guild, path, commandId)) {
                    val commandIdStr = if (commandId != null) " (id ${commandId})" else ""
                    logger.skip(path, "${it.simpleNestedName} rejected this command$commandIdStr")
                    return false
                }
            }
        }
        return true
    }

    protected fun ApplicationCommandBuilder<*>.fillApplicationCommandBuilder(func: KFunction<*>) {
        filters += AnnotationUtils.getFilters(context, func, ApplicationCommandFilter::class)

        if (func.hasAnnotationRecursive<NSFW>()) {
            throwArgument(func, "${annotationRef<NSFW>()} can only be used on text commands, use the #nsfw method on your annotation instead")
        }
    }

    private enum class TestState {
        INCLUDE,
        EXCLUDE,
        NO_ANNOTATION
    }
}

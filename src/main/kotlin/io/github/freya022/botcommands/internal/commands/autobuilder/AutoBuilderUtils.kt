package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.text.annotations.NSFW
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.commands.SkipLogger
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.ApplicationFunctionMetadata
import io.github.freya022.botcommands.internal.commands.application.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.commands.autobuilder.metadata.MetadataFunctionHolder
import io.github.freya022.botcommands.internal.commands.ratelimit.readRateLimit
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

//This is used so commands can't prevent other commands from being registered when an exception happens
internal inline fun <T : MetadataFunctionHolder> Iterable<T>.forEachWithDelayedExceptions(crossinline block: (T) -> Unit) {
    var ex: Throwable? = null
    forEach { metadata ->
        runCatching {
            block(metadata)
        }.onFailure {
            val newException = RuntimeException("An exception occurred while processing function ${metadata.func.shortSignature}", it)
            if (ex == null) {
                ex = newException
            } else {
                ex!!.addSuppressed(newException)
            }
        }
    }

    if (ex != null) {
        ex!!.rethrow("Exception(s) occurred while registering annotated commands")
    }
}

context(CommandAutoBuilder, SkipLogger)
internal fun runFiltered(
    manager: AbstractApplicationCommandManager,
    forceGuildCommands: Boolean,
    applicationFunctionMetadata: ApplicationFunctionMetadata<*>,
    scope: CommandScope,
    block: () -> Unit
) {
    val path = applicationFunctionMetadata.path
    val instance = applicationFunctionMetadata.instance
    val commandId = applicationFunctionMetadata.commandId
    val func = applicationFunctionMetadata.func

    // On global manager, do not register any command if forceGuildCommands is enabled,
    // as none of them would be global
    if (manager is GlobalApplicationCommandManager && forceGuildCommands)
        return

    // If guild commands aren't forced, check the scope
    if (!forceGuildCommands && !manager.isValidScope(scope)) return

    if (!checkDeclarationFilter(manager, func, path, commandId))
        return // Already logged

    if (commandId != null && !checkCommandId(manager, instance, commandId, path))
        return skip(path, "Guild does not support that command ID")

    val testState = checkTestCommand(manager, func, scope, manager.context)
    if (scope.isGlobal && testState != TestState.NO_ANNOTATION)
        throwInternal("Test commands on a global scope should have thrown in ${::checkTestCommand.shortSignatureNoSrc}")

    if (testState == TestState.EXCLUDE)
        return skip(path, "Is a test command while this guild isn't a test guild")

    block()
}

context(CommandAutoBuilder, SkipLogger)
internal fun checkDeclarationFilter(
    manager: AbstractApplicationCommandManager,
    func: KFunction<*>,
    path: CommandPath,
    commandId: String?,
): Boolean {
    func.findAllAnnotations<DeclarationFilter>().forEach { declarationFilter ->
        checkAt(manager is GuildApplicationCommandManager, func) {
            "${annotationRef<DeclarationFilter>()} can only be used on guild commands"
        }

        declarationFilter.filters.forEach {
            if (!serviceContainer.getService(it).filter(manager.guild, path, commandId)) {
                val commandIdStr = if (commandId != null) " (id ${commandId})" else ""
                skip(path, "${it.simpleNestedName} rejected this command$commandIdStr")
                return false
            }
        }
    }
    return true
}

@Suppress("DEPRECATION")
internal fun checkCommandId(manager: AbstractApplicationCommandManager, instance: ApplicationCommand, commandId: String, path: CommandPath): Boolean {
    if (manager is GuildApplicationCommandManager) {
        val guildIds = instance.getGuildsForCommandId(commandId, path) ?: return true

        if (manager.guild.idLong !in guildIds) {
            return false //Don't push command if it isn't allowed
        }
    }

    return true
}

internal enum class TestState {
    INCLUDE,
    EXCLUDE,
    NO_ANNOTATION
}

internal fun checkTestCommand(manager: AbstractApplicationCommandManager, func: KFunction<*>, scope: CommandScope, context: BContext): TestState {
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

internal fun CommandBuilder.fillCommandBuilder(functions: List<KFunction<*>>) {
    declarationSite = functions.first().let(DeclarationSite::fromFunctionSignature)

    val rateLimiter = functions.singleValueOfVariants("their rate limit specification") { readRateLimit(it) }
    val rateLimitRef = functions.singleAnnotationOfVariants<RateLimitReference>()

    // A single one of them can be used - One of them needs to be null
    check(rateLimitRef == null || rateLimiter == null) {
        "You can either define a rate limit or reference one, but not both"
    }

    if (rateLimiter != null) {
        rateLimit(rateLimiter) {
            declarationSite = this@fillCommandBuilder.declarationSite
        }
    }

    if (rateLimitRef != null) {
        rateLimitReference(rateLimitRef.group)
    }

    functions
        .singleValueOfVariants("user permission") { f ->
            AnnotationUtils.getUserPermissions(f).takeIf { it.isNotEmpty() }
        }
        ?.let { userPermissions = it }
    functions
        .singleValueOfVariants("bot permissions") { f ->
            AnnotationUtils.getBotPermissions(f).takeIf { it.isNotEmpty() }
        }
        ?.let { botPermissions = it }
}

internal fun CommandBuilder.fillCommandBuilder(func: KFunction<*>) = fillCommandBuilder(listOf(func))

context(CommandBuilder)
internal inline fun <reified A : Annotation> Iterable<KFunction<*>>.singlePresentAnnotationOfVariants(): Boolean {
    return singleAnnotationOfVariants<A>() != null
}

context(CommandBuilder)
internal inline fun <reified A : Annotation> Iterable<KFunction<*>>.singleAnnotationOfVariants(): A? {
    return singleValueOfVariants(annotationRef<A>()) { it.findAnnotationRecursive<A>() }
}

context(CommandBuilder)
internal fun <V : Any> Iterable<KFunction<*>>.singleValueOfVariants(desc: String, associationBlock: (KFunction<*>) -> V?): V? {
    val allValues = this.associateWith(associationBlock)

    val nonNullMap = allValues.filterValues { it != null }
    check(nonNullMap.size <= 1) {
        val refs = nonNullMap.map { it.key }.joinAsList { it.shortSignature }
        "Command '$path' should have $desc defined at most once:\n$refs"
    }
    return nonNullMap.values.firstOrNull()
}

@Suppress("UNCHECKED_CAST")
internal fun KFunction<*>.castFunction() = this as KFunction<Any>

internal fun ApplicationCommandBuilder<*>.fillApplicationCommandBuilder(func: KFunction<*>) {
    filters += AnnotationUtils.getFilters(context, func, ApplicationCommandFilter::class)

    if (func.hasAnnotationRecursive<NSFW>()) {
        throwArgument(func, "${annotationRef<NSFW>()} can only be used on text commands, use the #nsfw method on your annotation instead")
    }
}

internal fun CommandAutoBuilder.requireServiceOptionOrOptional(func: KFunction<*>, parameterAdapter: ParameterAdapter, commandAnnotation: KClass<out Annotation>) {
    if (parameterAdapter.isOptionalOrNullable) return

    val serviceError = serviceContainer.canCreateWrappedService(parameterAdapter.valueParameter) ?: return
    val originalParameter = parameterAdapter.originalParameter
    throwArgument(
        func,
        "Cannot determine usage of option '${originalParameter.bestName}' (${originalParameter.type.simpleNestedName}) and service loading failed, " +
                "if this is a Discord option, use @${optionAnnotation.simpleNestedName}, check @${commandAnnotation.simpleNestedName} for more details\n" +
                serviceError.toDetailedString()
    )
}
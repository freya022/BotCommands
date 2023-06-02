package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.FirstReadyEvent
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.simpleNestedName
import mu.KotlinLogging
import kotlin.reflect.full.declaredMemberFunctions

private val logger = KotlinLogging.logger { }
private val commandAnnotations = listOf(
    JDASlashCommand::class,
    JDAUserCommand::class,
    JDAMessageCommand::class,
    JDATextCommand::class,
    AppDeclaration::class,
    TextDeclaration::class
)

@BService
class CommandsPresenceChecker {
    @BEventListener
    fun onFirstReady(event: FirstReadyEvent, context: BContextImpl) {
        context.serviceAnnotationsMap
            .getClassesWithAnnotation<Command>()
            .forEach { clazz ->
                val hasDeclarations = clazz.declaredMemberFunctions.any { function ->
                    function.annotations.any { it.annotationClass in commandAnnotations }
                }
                if (!hasDeclarations) {
                    logger.warn("Found no command declaration in class ${clazz.simpleNestedName} annotated with @${Command::class.simpleNestedName}")
                }
            }
    }
}
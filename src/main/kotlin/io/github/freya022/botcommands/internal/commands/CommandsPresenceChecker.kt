package io.github.freya022.botcommands.internal.commands

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }
private val commandAnnotations = listOf(
    JDASlashCommand::class.jvmName,
    JDAUserCommand::class.jvmName,
    JDAMessageCommand::class.jvmName,
    JDATextCommand::class.jvmName,
    AppDeclaration::class.jvmName,
    TextDeclaration::class.jvmName
)

//This checker works on all classes from the user packages, but only on "services" of internal classes
class CommandsPresenceChecker : ClassGraphProcessor {
    private val noDeclarationClasses: MutableList<String> = arrayListOf()
    private val noAnnotationMethods: MutableList<MethodInfo> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        val isCommand = classInfo.hasAnnotation(Command::class.java)
        val commandDeclarations = classInfo.declaredMethodInfo
            .filterNot { it.isSynthetic }
            .filter { function ->
                function.annotationInfo.any { it.name in commandAnnotations }
            }

        if (isCommand && commandDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortSignature
        } else if (!isCommand && commandDeclarations.isNotEmpty()) {
            // If there is no command annotation but command declarations were found
            noAnnotationMethods += commandDeclarations
        }
    }

    override fun postProcess(context: BContext) {
        if (noDeclarationClasses.isNotEmpty()) {
            logger.warn("Some classes annotated with @${Command::class.simpleNestedName} were found to have no command declarations:\n${
                noDeclarationClasses.joinAsList()
            }")
        }

        if (noAnnotationMethods.isNotEmpty()) {
            throw IllegalStateException("Some command declarations do not have their declaring class annotated with @${Command::class.simpleNestedName}:\n${
                noAnnotationMethods.joinAsList()
            }")
        }
    }
}
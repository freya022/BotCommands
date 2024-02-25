package io.github.freya022.botcommands.internal.commands

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandsDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GuildApplicationCommandsDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.TextDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedReference
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }
private val commandAnnotations = listOf(
    JDASlashCommand::class.jvmName,
    JDAUserCommand::class.jvmName,
    JDAMessageCommand::class.jvmName,
    JDATextCommandVariation::class.jvmName,
    TextDeclaration::class.jvmName
)
private val declarationInterfaces = listOf(
    GuildApplicationCommandsDeclaration::class.jvmName,
    GlobalApplicationCommandsDeclaration::class.jvmName,
)

//This checker works on all classes from the user packages, but only on "services" of internal classes
class CommandsPresenceChecker : ClassGraphProcessor {
    private val noDeclarationClasses: MutableList<String> = arrayListOf()
    private val noAnnotationMethods: MutableList<MethodInfo> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        val isCommand = classInfo.hasAnnotation(Command::class.java)
        val hasDeclarationInterfaces = declarationInterfaces.any { classInfo.implementsInterface(it) }
        val commandDeclarations by lazy {
            classInfo.declaredMethodInfo
                .filterNot { it.isSynthetic }
                .filter { function ->
                    function.annotationInfo.any { it.name in commandAnnotations }
                }
        }

        if (isCommand && !hasDeclarationInterfaces && commandDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortQualifiedReference
        } else if (!isCommand && commandDeclarations.isNotEmpty()) {
            // If there is no command annotation but command declarations were found
            noAnnotationMethods += commandDeclarations
        }
    }

    override fun postProcess(context: BContext) {
        if (noDeclarationClasses.isNotEmpty()) {
            logger.warn {
                val refs = noDeclarationClasses.joinAsList()
                "Some classes annotated with ${annotationRef<Command>()} were found to have no command declarations:\n$refs"
            }
        }

        check(noAnnotationMethods.isEmpty()) {
            val refs = noAnnotationMethods.joinAsList { it.shortSignature }
            "Some command declarations do not have their declaring class annotated with ${annotationRef<Command>()}:\n$refs"
        }
    }
}
package io.github.freya022.botcommands.internal.commands

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

// TODO add tests so those names stay up to date
private val logger = KotlinLogging.logger { }
private val commandAnnotations = listOf(
    JDASlashCommand::class.jvmName,
    JDAUserCommand::class.jvmName,
    JDAMessageCommand::class.jvmName,
    "io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation"
)
private val commandProviderInterfaces = listOf(
    "io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider",
    GuildApplicationCommandProvider::class.jvmName,
    GlobalApplicationCommandProvider::class.jvmName,
)

//This checker works on all classes from the user packages, but only on "services" of internal classes
internal class CommandsPresenceChecker : ClassGraphProcessor {
    private val noDeclarationClasses: MutableList<String> = arrayListOf()
    private val noAnnotationMethods: MutableList<MethodInfo> = arrayListOf()

    override fun processClass(
        serviceContainer: ServiceContainer,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isService: Boolean
    ) {
        if (classInfo.isAbstract) return

        val isCommand = classInfo.hasAnnotation(Command::class.java)
        val hasCommandProviderInterfaces = commandProviderInterfaces.any { classInfo.implementsInterface(it) }
        val commandDeclarations by lazy {
            classInfo.declaredMethodInfo
                .filterNot { it.isSynthetic }
                .filter { function ->
                    function.annotationInfo.any { it.name in commandAnnotations }
                }
        }

        if (isCommand && !hasCommandProviderInterfaces && commandDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortQualifiedName
        } else if (!isCommand && commandDeclarations.isNotEmpty()) {
            // If there is no command annotation but command declarations were found
            noAnnotationMethods += commandDeclarations
        }
    }

    override fun postProcess(serviceContainer: ServiceContainer) {
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

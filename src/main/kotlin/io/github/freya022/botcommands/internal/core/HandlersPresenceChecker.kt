package io.github.freya022.botcommands.internal.core

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedReference
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }
private val handlerAnnotations = listOf(
    JDAButtonListener::class.jvmName,
    JDASelectMenuListener::class.jvmName,
    AutocompleteHandler::class.jvmName,
    ModalHandler::class.jvmName
)

internal class HandlersPresenceChecker : ClassGraphProcessor {
    private val noDeclarationClasses: MutableList<String> = arrayListOf()
    private val noAnnotationMethods: MutableList<MethodInfo> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        val isCommand = classInfo.hasAnnotation(Command::class.java)
        val isHandler = classInfo.hasAnnotation(Handler::class.java)
        val isHandlerOrCommand = isHandler || isCommand

        val handlerDeclarations = classInfo.declaredMethodInfo
            .filterNot { it.isSynthetic }
            .filter { function ->
                function.annotationInfo.any { it.name in handlerAnnotations }
            }

        if (isHandler && handlerDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortQualifiedReference
        } else if (!isHandlerOrCommand && handlerDeclarations.isNotEmpty()) {
            // If there is no handler annotation but handler declarations were found
            noAnnotationMethods += handlerDeclarations
        }
    }

    override fun postProcess(context: BContext) {
        if (noDeclarationClasses.isNotEmpty()) {
            logger.warn {
                val refs = noDeclarationClasses.joinAsList()
                "Some classes annotated with ${annotationRef<Handler>()} were found to have no handler declarations:\n$refs"
            }
        }

        check(noAnnotationMethods.isEmpty()) {
            val refs = noAnnotationMethods.joinAsList { it.shortSignature }
            "Some handler declarations do not have their declaring class annotated with ${annotationRef<Handler>()}:\n$refs"
        }
    }
}
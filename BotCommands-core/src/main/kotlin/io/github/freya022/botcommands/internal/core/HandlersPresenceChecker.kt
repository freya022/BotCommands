package io.github.freya022.botcommands.internal.core

import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }
private val handlerAnnotations = listOf(
    // TODO test in respective modules that this is correct
    "io.github.freya022.botcommands.api.components.annotations.JDAButtonListener",
    "io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener",
    "io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler",
    "io.github.freya022.botcommands.api.modals.annotations.ModalHandler",
)

internal class HandlersPresenceChecker : ClassPathProcessor {
    private val noDeclarationClasses: MutableList<String> = arrayListOf()
    private val noAnnotationMethods: MutableList<MethodInfo> = arrayListOf()

    override fun processClass(data: ClassPathProcessor.ClassData) {
        val classInfo = data.classInfo
        if (classInfo.isAbstract) return

        val isCommand = classInfo.hasAnnotation("io.github.freya022.botcommands.api.commands.annotations.Command")
        val isHandler = classInfo.hasAnnotation(Handler::class.java)
        val isHandlerOrCommand = isHandler || isCommand

        val handlerDeclarations = classInfo.declaredMethodInfo
            .filterNot { it.isSynthetic }
            .filter { function ->
                function.annotationInfo.any { it.name in handlerAnnotations }
            }

        if (isHandler && handlerDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortQualifiedName
        } else if (!isHandlerOrCommand && handlerDeclarations.isNotEmpty()) {
            // If there is no handler annotation but handler declarations were found
            noAnnotationMethods += handlerDeclarations
        }
    }

    override fun postProcess(data: ClassPathProcessor.PostProcessData) {
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

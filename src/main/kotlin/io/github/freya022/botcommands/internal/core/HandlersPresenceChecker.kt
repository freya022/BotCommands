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
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import mu.KotlinLogging
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

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        val isCommand = classInfo.hasAnnotation(Command::class.java)
        val isHandler = classInfo.hasAnnotation(Handler::class.java)
        val isService = isHandler || isCommand

        val handlerDeclarations = classInfo.declaredMethodInfo
            .filterNot { it.isSynthetic }
            .filter { function ->
                function.annotationInfo.any { it.name in handlerAnnotations }
            }

        if (isHandler && handlerDeclarations.isEmpty()) {
            noDeclarationClasses += classInfo.shortSignature
        } else if (!isService && handlerDeclarations.isNotEmpty()) {
            // If there is no handler annotation but handler declarations were found
            noAnnotationMethods += handlerDeclarations
        }
    }

    override fun postProcess(context: BContext) {
        if (noDeclarationClasses.isNotEmpty()) {
            logger.warn("Some classes annotated with @${Handler::class.simpleNestedName} were found to have no handler declarations:\n${
                noDeclarationClasses.joinAsList()
            }")
        }

        if (noAnnotationMethods.isNotEmpty()) {
            throw IllegalStateException("Some handler declarations do not have their declaring class annotated with @${Handler::class.simpleNestedName}:\n${
                noAnnotationMethods.joinAsList { it.shortSignature }
            }")
        }
    }
}
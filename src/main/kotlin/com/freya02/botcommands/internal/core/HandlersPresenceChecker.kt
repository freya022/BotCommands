package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.annotations.JDASelectMenuListener
import com.freya02.botcommands.api.core.annotations.Handler
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.internal.utils.shortSignature
import com.freya02.botcommands.internal.utils.toShortSignature
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
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

class HandlersPresenceChecker : ClassGraphProcessor {
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
            noDeclarationClasses += classInfo.toShortSignature(kClass)
        } else if (!isService && handlerDeclarations.isNotEmpty()) {
            // If there is no handler annotation but handler declarations were found
            noAnnotationMethods += handlerDeclarations
        }
    }

    override fun postProcess(context: BContext) {
        if (noDeclarationClasses.isNotEmpty()) {
            logger.warn("Some classes annotated with @${Handler::class.simpleNestedName} were found to have no handler declarations:\n${
                noDeclarationClasses.joinToString(prefix = " - ", separator = "\n - ")
            }")
        }

        if (noAnnotationMethods.isNotEmpty()) {
            throw IllegalStateException("Some handler declarations do not have their declaring class annotated with @${Handler::class.simpleNestedName}:\n${
                noAnnotationMethods.joinToString(prefix = " - ", separator = "\n - ") { it.shortSignature }
            }")
        }
    }
}
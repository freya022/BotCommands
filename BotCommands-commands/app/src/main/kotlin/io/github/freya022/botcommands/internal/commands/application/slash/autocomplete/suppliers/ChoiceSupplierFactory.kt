package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.suppliers

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteTransformer
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.collectionElementType
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

private val primitiveChoiceTypes: List<Class<*>> = listOf(String::class.java, Long::class.java, Double::class.java)

@BService
@RequiresApplicationCommands
internal class ChoiceSupplierFactory internal constructor(
    private val transformers: List<AutocompleteTransformer<Any>>,
) {

    internal fun create(
        function: KFunction<Collection<Any>>,
        mode: AutocompleteMode,
        showUserInput: Boolean,
    ): ChoiceSupplier {
        val collectionElementType = function.returnType.collectionElementType?.jvmErasure?.java
            ?: throwArgument(function, "Unable to determine return type, it should inherit Collection")

        //accommodate for user input
        val maxChoices = OptionData.MAX_CHOICES - if (showUserInput) 1 else 0
        return when {
            collectionElementType in primitiveChoiceTypes -> generateSupplierFromStrings(mode, maxChoices)
            collectionElementType.isSubclassOf<Command.Choice>() -> ChoiceSupplierChoices(maxChoices)
            else -> generateWithTransformer(function, maxChoices, collectionElementType)
        }
    }

    private fun generateSupplierFromStrings(autocompleteMode: AutocompleteMode, maxChoices: Int): ChoiceSupplier {
        return if (autocompleteMode == AutocompleteMode.FUZZY) {
            ChoiceSupplierStringFuzzy(maxChoices)
        } else {
            ChoiceSupplierStringContinuity(maxChoices)
        }
    }

    private fun generateWithTransformer(
        function: KFunction<Collection<Any>>,
        maxChoices: Int,
        collectionElementType: Class<out Any>,
    ): ChoiceSupplierTransformer {
        val transformer = transformers.firstOrNull { it.elementType == collectionElementType }
        requireAt(transformer != null, function) {
            """
                No autocomplete transformer has been registered for objects of type '${collectionElementType.simpleName}'.
                Registered transformers: ${transformers.joinToString { it.javaClass.shortQualifiedName }}
                You may also check the docs for ${classRef<AutocompleteHandler>()} and ${classRef<AutocompleteTransformer<*>>()}
            """.trimIndent()
        }

        return ChoiceSupplierTransformer(transformer, maxChoices)
    }
}

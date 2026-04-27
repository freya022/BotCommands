package io.github.freya022.botcommands.internal.commands.text.options

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

internal class TextGeneratedOption internal constructor(
    override val parent: TextCommandParameter,
    generatedOptionBuilder: TextGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {

    override val executable get() = parent.executable

    private val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier

    internal fun getCheckedDefaultValue(event: BaseCommandEvent): Any? {
        val generatedValue = generatedValueSupplier.getDefaultValue(event)
        checkDefaultValue(generatedValue)
        return generatedValue
    }

    private fun checkDefaultValue(defaultValue: Any?) {
        if (defaultValue != null) {
            val expectedType: KClass<*> = this.type.jvmErasure
            requireAt(expectedType.isInstance(defaultValue), this.executable.declarationSite) {
                "Generated value supplier for parameter #${this.index} has returned a default value of type ${defaultValue.javaClass.simpleName} but a value of type ${expectedType.simpleName} was expected"
            }
        } else {
            requireAt(this.isOptionalOrNullable, this.executable.declarationSite) {
                "Generated value supplier for parameter #${this.index} has returned a null value but parameter is not optional"
            }
        }
    }
}

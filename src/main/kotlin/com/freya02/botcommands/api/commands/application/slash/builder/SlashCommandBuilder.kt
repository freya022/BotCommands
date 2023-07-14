package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.fakeSlashFunction
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.findDeclarationName
import com.freya02.botcommands.internal.utils.throwUser
import com.freya02.botcommands.internal.utils.toDiscordString
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>(name, function ?: fakeSlashFunction) {
    /**
     * **Annotation equivalent:** [JDASlashCommand.description]
     *
     * @see JDASlashCommand.description
     */
    var description: String = DEFAULT_DESCRIPTION

    protected abstract val allowOptions: Boolean
    protected abstract val allowSubcommands: Boolean
    protected abstract val allowSubcommandGroups: Boolean

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    fun inlineClassOption(declaredName: String, optionName: String? = null, clazz: Class<*>, block: SlashCommandOptionBuilder.() -> Unit) {
        val aggregatorConstructor = clazz.kotlin.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            option(parameterName, optionName ?: parameterName.toDiscordString(), block)
        }
    }

    inline fun <reified T : Any> inlineClassOption(declaredName: String, optionName: String? = null, noinline block: SlashCommandOptionBuilder.() -> Unit) {
        inlineClassOption(declaredName, optionName, T::class.java, block)
    }

    /**
     * **Annotation equivalent:** [VarArgs]
     */
    fun inlineClassOptionVararg(declaredName: String, clazz: Class<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        val aggregatorConstructor = clazz.kotlin.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            nestedOptionVararg(parameterName, amount, requiredAmount, optionNameSupplier, block)
        }
    }

    /**
     * **Annotation equivalent:** [VarArgs]
     */
    inline fun <reified T : Any> inlineClassOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, noinline optionNameSupplier: (Int) -> String, noinline block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        inlineClassOptionVararg(declaredName, T::class.java, amount, requiredAmount, optionNameSupplier, block)
    }

    /**
     * **Annotation equivalent:** [VarArgs]
     */
    @JvmOverloads
    fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        //Same as in TextCommandVariationBuilder#optionVararg
        varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    isOptional = i >= requiredAmount
                }
            }
        }
    }

    override fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): SlashCommandOptionAggregateBuilder {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        return SlashCommandOptionAggregateBuilder(context, aggregatorParameter, aggregator)
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"
    }
}

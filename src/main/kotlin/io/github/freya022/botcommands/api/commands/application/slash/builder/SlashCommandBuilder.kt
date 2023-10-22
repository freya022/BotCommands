package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.fakeSlashFunction
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.freya022.botcommands.internal.utils.toDiscordString
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

abstract class SlashCommandBuilder internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>(context, name, function ?: fakeSlashFunction) {
    override val type: CommandType = CommandType.SLASH

    /**
     * Short description of the command displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped, example: `ban.description`.
     *
     * @see LocalizationFunction
     *
     * @see JDASlashCommand.description
     */
    var description: String = ""

    protected abstract val allowOptions: Boolean
    protected abstract val allowSubcommands: Boolean
    protected abstract val allowSubcommandGroups: Boolean

    init {
        Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Text command name")
    }

    /**
     * Declares an input option, see supported types at [ParameterResolver],
     * additional resolvers can be implemented with [SlashParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [function]
     * @param optionName   Name of the option on Discord,
     * transforms all uppercase characters with underscore + lowercase by default
     */
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    /**
     * Declares an input option encapsulated in an inline class, see supported types at [ParameterResolver],
     * additional resolvers can be implemented with [SlashParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [function]
     * @param optionName   Name of the option on Discord,
     * transforms all uppercase characters with underscore + lowercase by default
     * @param clazz        The inline class type
     */
    fun inlineClassOption(declaredName: String, optionName: String? = null, clazz: KClass<*>, block: SlashCommandOptionBuilder.() -> Unit = {}) {
        val aggregatorConstructor = clazz.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            option(parameterName, optionName ?: parameterName.toDiscordString(), block)
        }
    }

    inline fun <reified T : Any> inlineClassOption(declaredName: String, optionName: String? = null, noinline block: SlashCommandOptionBuilder.() -> Unit = {}) {
        inlineClassOption(declaredName, optionName, T::class, block)
    }

    /**
     * @see VarArgs
     */
    fun inlineClassOptionVararg(declaredName: String, clazz: KClass<*>, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        val aggregatorConstructor = clazz.primaryConstructor
            ?: throwUser("Found no public constructor for class ${clazz.simpleNestedName}")
        aggregate(declaredName, aggregatorConstructor) {
            val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
                ?: throwUser(aggregatorConstructor, "Constructor must only have one parameter")
            nestedOptionVararg(parameterName, amount, requiredAmount, optionNameSupplier, block)
        }
    }

    /**
     * @see VarArgs
     */
    inline fun <reified T : Any> inlineClassOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, noinline optionNameSupplier: (Int) -> String, noinline block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        inlineClassOptionVararg(declaredName, T::class, amount, requiredAmount, optionNameSupplier, block)
    }

    /**
     * @see VarArgs
     */
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

        return SlashCommandOptionAggregateBuilder(context, this, aggregatorParameter, aggregator)
    }
}

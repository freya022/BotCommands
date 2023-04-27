package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.fakeSlashFunction
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KFunction

abstract class SlashCommandBuilder internal constructor(
    protected val context: BContextImpl,
    name: String,
    function: KFunction<Any>? //Nullable as subcommands make top level commands impossible to execute
) : ApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>(name, function ?: fakeSlashFunction) {
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
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

    override fun constructAggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>): SlashCommandOptionAggregateBuilder {
        if (!allowOptions) throwUser("Cannot add options as this already contains subcommands/subcommand groups")

        return SlashCommandOptionAggregateBuilder(context, owner, declaredName, aggregator)
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "No description"
    }
}

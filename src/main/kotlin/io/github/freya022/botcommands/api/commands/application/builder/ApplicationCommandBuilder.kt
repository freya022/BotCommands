package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.utils.reference
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder<T>> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>
) : ExecutableCommandBuilder<T, Any>(context, name, function) {
    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    val filters: MutableList<ApplicationCommandFilter<*>> = arrayListOf()

    /**
     * Specifies whether the application command is usable in NSFW channels.<br>
     * Note: NSFW commands need to be enabled by the user in order to appear in DMs
     *
     * **Default:** false
     *
     * See the [Age-Restricted Commands FAQ](https://support.discord.com/hc/en-us/articles/10123937946007) for more details.
     *
     * @return `true` if the command is restricted to NSFW channels
     *
     * @see JDASlashCommand.nsfw
     * @see JDAUserCommand.nsfw
     * @see JDAMessageCommand.nsfw
     */
    var nsfw: Boolean = false

    /**
     * Declares a custom option, such as an [AppLocalizationContext] (with [@LocalizationBundle][LocalizationBundle])
     * or a service.
     *
     * Additional types can be added by implementing [ICustomResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * Declares a generated option, the supplier gets called on each command execution.
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     *
     * @see GeneratedOption @GeneratedOption
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }
}

inline fun <reified T : ApplicationCommandFilter<*>> ApplicationCommandBuilder<*>.filter(): T {
    val filter = context.getService<T>()
    require(!filter.global) {
        "Global filters cannot be used explicitly, see ${Filter::global.reference}"
    }
    return filter
}
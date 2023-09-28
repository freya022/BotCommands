package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.Resolvers.toHumanName
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.components.ComponentDescriptor
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KParameter

internal class EnumResolver<E : Enum<E>> internal constructor(
    e: Class<E>,
    private val values: Array<out E>,
    private val nameFunction: EnumNameFunction<E>
) :
    ParameterResolver<EnumResolver<E>, E>(e),
    RegexParameterResolver<EnumResolver<E>, E>,
    SlashParameterResolver<EnumResolver<E>, E>,
    ComponentParameterResolver<EnumResolver<E>, E> {

    //region Regex
    override val pattern: Pattern = Pattern.compile("(?i)(${values.joinToString("|") { Pattern.quote(nameFunction.apply(it)) }})(?-i)")

    override val testExample: String = values.first().name

    override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return nameFunction.apply(values.first())
    }

    override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): E = values.first { it.name.contentEquals(args[0], ignoreCase = true) }
    //endregion

    //region Slash
    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return values.map { Choice(nameFunction.apply(it), it.name) }
    }

    override suspend fun resolveSuspend(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): E = values.first { it.name == optionMapping.asString }
    //endregion

    //region Component
    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): E = values.first { it.name == arg }
    //endregion
}

fun interface EnumNameFunction<E : Enum<E>> {
    fun apply(value: E): String
}

/**
 * Utility factories to create commonly used parameter resolvers.
 */
object Resolvers {
    /**
     * Creates an enum resolver for [text][RegexParameterResolver]/[slash][SlashParameterResolver] commands,
     * as well as [component data][ComponentParameterResolver].
     *
     * The created resolver needs to be registered either by calling [ResolverContainer.addResolver],
     * or by using a service factory with [Resolver] as such:
     *
     * ```java
     * public class EnumResolvers {
     *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
     *     @Resolver
     *     public ParameterResolver<?, ?> timeUnitResolver() {
     *         return Resolvers.enumResolver(TimeUnit.class, TimeUnit.values());
     *     }
     *
     *     ...other resolvers...
     * }
     * ```
     *
     * **Note:** you have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
     *
     * @param values       the accepted enumeration values
     * @param nameFunction the function transforming the enum value into the display name
     */
    @JvmStatic
    @JvmOverloads
    fun <E : Enum<E>> enumResolver(
        e: Class<E>,
        values: Array<out E>,
        nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }
    ): ParameterResolver<*, E> {
        return EnumResolver(e, values, nameFunction)
    }

    @JvmStatic
    @JvmOverloads
    fun <E : Enum<E>> toHumanName(value: E, locale: Locale = Locale.ROOT): String {
        return value.name.lowercase(locale).replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Creates an enum resolver for [text][RegexParameterResolver]/[slash][SlashParameterResolver] commands,
 * as well as [component data][ComponentParameterResolver].
 *
 * The created resolver needs to be registered either by calling [ResolverContainer.addResolver],
 * or by using a service factory with [Resolver] as such:
 *
 * ```kt
 * object EnumResolvers {
 *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers.Enum#toHumanName'
 *     @Resolver
 *     fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)
 *
 *     ...other resolvers...
 * }
 * ```
 *
 * **Note:** you have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
 *
 * @param values       the accepted enumeration values
 * @param nameFunction the function transforming the enum value into the display name
 */
inline fun <reified E : Enum<E>> enumResolver(
    vararg values: E = enumValues(),
    noinline nameFunction: (e: E) -> String = { it.toHumanName() }
): ParameterResolver<*, E> = Resolvers.enumResolver(E::class.java, values, nameFunction)

fun <E : Enum<E>> E.toHumanName(locale: Locale = Locale.ROOT): String = toHumanName(this, locale)
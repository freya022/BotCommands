package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.Resolvers.toHumanName
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.commands.text.TextCommandVariation
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor
import io.github.freya022.botcommands.internal.utils.throwInternal
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
    private val values: Collection<E>,
    private val guildValuesSupplier: EnumValuesSupplier<E>,
    private val nameFunction: EnumNameFunction<E>
) :
    ClassParameterResolver<EnumResolver<E>, E>(e),
    TextParameterResolver<EnumResolver<E>, E>,
    SlashParameterResolver<EnumResolver<E>, E>,
    ComponentParameterResolver<EnumResolver<E>, E> {

    // Key is both the enum name and the human name
    private val enumMap: Map<String, E> = buildMap {
        e.enumConstants.forEach {
            this[it.name.lowercase()] = it
            this[nameFunction.apply(it).lowercase()] = it
        }
    }

    //region Regex
    override val pattern: Pattern = Pattern.compile("(?i)(${values.joinToString("|") { Pattern.quote(nameFunction.apply(it)) }})(?-i)")

    override val testExample: String = values.first().name

    private val helpExample = nameFunction.apply(values.first())
    override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String = helpExample

    override suspend fun resolveSuspend(
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): E? = getEnumValueOrNull(args[0]!!)
    //endregion

    //region Slash
    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return guildValuesSupplier.get(guild).map { Choice(nameFunction.apply(it), it.name) }
    }

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): E = getEnumValue(optionMapping.asString)
    //endregion

    //region Component
    override suspend fun resolveSuspend(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): E? = getEnumValueOrNull(arg)
    //endregion

    override fun toString(): String {
        return "EnumResolver(values=$values, guildValuesSupplier=$guildValuesSupplier, nameFunction=$nameFunction)"
    }

    private fun getEnumValue(name: String): E = getEnumValueOrNull(name) ?: throwInternal("Could not find enum value '$name', map: $enumMap")
    private fun getEnumValueOrNull(name: String): E? = enumMap[name.lowercase()]
}

fun interface EnumNameFunction<E : Enum<E>> {
    fun apply(value: E): String
}

fun interface EnumValuesSupplier<E : Enum<E>> {
    /**
     * @param guild The guild containing the command, `null` for global commands
     */
    fun get(guild: Guild?): Collection<E>
}

/**
 * Utility factories to create commonly used parameter resolvers.
 */
object Resolvers {
    /**
     * Creates an enum resolver for [text][TextParameterResolver]/[slash][SlashParameterResolver] commands,
     * as well as [component data][ComponentParameterResolver].
     *
     * Text command options are case-insensitive.
     *
     * The created resolver needs to be registered either by calling [ResolverContainer.addResolver],
     * or by using a service factory with [Resolver] as such:
     *
     * ```java
     * public class EnumResolvers {
     *     // Resolver for DAYS/HOURS/MINUTES, where the displayed name is given by 'Resolvers#toHumanName'
     *     @Resolver
     *     public ParameterResolver<?, ?> timeUnitResolver() {
     *         return Resolvers.enumResolver(TimeUnit.class, EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES));
     *     }
     *
     *     ...other resolvers...
     * }
     * ```
     *
     * **Note:** You have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
     *
     * @param values              The accepted enumeration values
     * @param guildValuesSupplier The function retrieving the enum values depending on the [Guild]
     * @param nameFunction        The function transforming the enum value into the display name, uses [toHumanName] by default
     *
     * @see toHumanName
     */
    @JvmStatic
    @JvmOverloads //TODO use a builder pattern
    fun <E : Enum<E>> enumResolver(
        e: Class<E>,
        values: Collection<E> = e.enumConstants.toCollection(EnumSet.noneOf(e)),
        guildValuesSupplier: EnumValuesSupplier<E> = EnumValuesSupplier { values },
        nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }
    ): ClassParameterResolver<*, E> {
        return EnumResolver(e, values, guildValuesSupplier, nameFunction)
    }

    @JvmStatic
    @JvmOverloads
    fun toHumanName(value: Enum<*>, locale: Locale = Locale.ROOT): String {
        return value.name.lowercase(locale).replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Creates an enum resolver for [text][TextParameterResolver]/[slash][SlashParameterResolver] commands,
 * as well as [component data][ComponentParameterResolver].
 *
 * Text command options are case-insensitive.
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
 * **Note:** You have to enable [SlashOption.usePredefinedChoices] in order for the choices to appear.
 *
 * @param values              The accepted enumeration values
 * @param guildValuesSupplier The function retrieving the enum values depending on the [Guild]
 * @param nameFunction        The function transforming the enum value into the display name, uses [toHumanName] by default
 *
 * @see toHumanName
 */
inline fun <reified E : Enum<E>> enumResolver(
    vararg values: E = enumValues(),
    guildValuesSupplier: EnumValuesSupplier<E> = defaultEnumValuesSupplier<E>(values.toCollection(enumSetOf<E>())),
    noinline nameFunction: (e: E) -> String = { it.toHumanName() }
): ClassParameterResolver<*, E> = Resolvers.enumResolver(E::class.java, values.toCollection(enumSetOf<E>()), guildValuesSupplier, nameFunction)

// Cannot directly inline this. It is used as a way to not make a copy of the values for every guild
@PublishedApi
internal inline fun <reified E : Enum<E>> defaultEnumValuesSupplier(valueCollection: Collection<E>) =
    EnumValuesSupplier { valueCollection }

/**
 * Convert an enum to a more human-friendly name.
 *
 * This takes the enum value's name and capitalizes it, while replacing underscores with spaces.
 */
fun Enum<*>.toHumanName(locale: Locale = Locale.ROOT): String = toHumanName(this, locale)
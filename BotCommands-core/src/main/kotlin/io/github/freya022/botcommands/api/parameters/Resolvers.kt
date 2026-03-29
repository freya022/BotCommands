package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.Resolvers.toHumanName
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverBuilder
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.EnumResolverBuilderImpl
import io.github.freya022.botcommands.internal.utils.StackSensitive
import io.github.freya022.botcommands.internal.utils.currentFrame
import io.github.freya022.botcommands.internal.utils.findCaller
import io.github.freya022.botcommands.internal.utils.sourceFile
import java.util.*
import java.util.function.Consumer
import javax.annotation.CheckReturnValue

/**
 * Utility factories to create commonly used parameter resolvers.
 */
object Resolvers {
    /**
     * Creates an enum resolver for the provided enum type.
     * This supports no handler type by default, you need to explicitly register them,
     * each handler type as its own, for example, `SlashCommandEnumResolver`.
     *
     * ### Registration
     *
     * The returned object can be registered as a service factory, with [@Resolver][Resolver],
     * or be registered in a [ResolverProvider].
     *
     * @param enumType The enum type
     */
    @JvmStatic
    @CheckReturnValue
    fun <E : Enum<E>> ofEnum(enumType: Class<E>, block: Consumer<EnumResolverBuilder<E>>): ParameterResolverFactory {
        @OptIn(StackSensitive::class)
        val caller = findCaller()
        val callerSig = "${caller.declaringClass.shortQualifiedName}.${caller.methodName.substringBefore('$')} (${caller.sourceFile}:${caller.lineNumber})"
        return EnumResolverBuilderImpl(enumType, callerSig).apply(block::accept).build()
    }

    /**
     * Creates an enum resolver for the provided enum type.
     * This supports no handler type by default, you need to explicitly register them,
     * each handler type as its own, for example, `SlashCommandEnumResolver`.
     *
     * ### Registration
     *
     * The returned object can be registered as a service factory, with [@Resolver][Resolver],
     * or be registered in a [ResolverProvider].
     *
     * @param E The enum type
     */
    @JvmSynthetic
    inline fun <reified E : Enum<E>> ofEnum(noinline block: EnumResolverBuilder<E>.() -> Unit): ParameterResolverFactory {
        return ofEnum(E::class.java, block)
    }

    /**
     * Convert an enum to a more human-friendly name.
     *
     * This takes the enum value's name and capitalizes it, while replacing underscores with spaces, for example,
     * `MY_ENUM_VALUE` -> `Enum value name`.
     */
    @JvmStatic
    @JvmOverloads
    fun toHumanName(value: Enum<*>, locale: Locale = Locale.ROOT): String {
        return value.name.lowercase(locale)
            .replace('_', ' ')
            .replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Convert an enum to a more human-friendly name.
 *
 * This takes the enum value's name and capitalizes it, while replacing underscores with spaces, for example,
 * `MY_ENUM_VALUE` -> `Enum value name`.
 */
fun Enum<*>.toHumanName(locale: Locale = Locale.ROOT): String = toHumanName(this, locale)

// TODO update docs
/**
 * Creates a [parameter resolver factory][ParameterResolverFactory] from the provided resolver [producer].
 *
 * The [producer] is called for each function parameter with the exact [R] type.
 *
 * This should be returned in a service factory, using [@ResolverFactory][ResolverFactory].
 *
 * Example using a custom localization service:
 * ```kt
 * @BConfiguration
 * object MyCustomLocalizationResolverProvider {
 *     // The parameter resolver, which will be created once per parameter
 *     class MyCustomLocalizationResolver(
 *         private val localizationService: LocalizationService,
 *         private val guildSettingsService: GuildSettingsService,
 *         private val bundleName: String,
 *         private val prefix: String?
 *     ) : ClassParameterResolver<MyCustomLocalizationResolver, MyCustomLocalization>(MyCustomLocalization::class),
 *         ICustomResolver<MyCustomLocalizationResolver, MyCustomLocalization> {
 *
 *         // Called when a command is used
 *         override suspend fun resolveSuspend(executable: Executable, event: Event): MyCustomLocalization {
 *             return if (event is Interaction) {
 *                 val guild = event.guild
 *                     ?: throw IllegalStateException("Cannot get localization outside of guilds")
 *                 // The root localization file not existing isn't an issue on production
 *                 val localization = localizationService.getInstance(bundleName, guildSettingsService.getGuildLocale(guild.idLong))
 *                     ?: throw IllegalArgumentException("No root bundle exists for '$bundleName'")
 *
 *                 // Return resolved object
 *                 MyCustomLocalization(localization, prefix)
 *             } else {
 *                 throw UnsupportedOperationException("Unsupported event: ${event.javaClass.simpleNestedName}")
 *             }
 *         }
 *     }
 *
 *     // Service factory returning a resolver factory
 *     // The returned factory is used on each command/handler parameter of type "MyCustomLocalization",
 *     // which is the same type as what MyCustomLocalizationResolver returns
 *     @ResolverFactory
 *     fun myCustomLocalizationResolverProvider(localizationService: LocalizationService, guildSettingsService: GuildSettingsService) = resolverFactory { parameter ->
 *         // Find @LocalizationBundle on the parameter
 *         val bundle = parameter.parameter.findAnnotation<LocalizationBundle>()
 *             ?: throw IllegalArgumentException("Parameter ${parameter.parameter} must be annotated with LocalizationBundle")
 *
 *         // Return our resolver for that parameter
 *         MyCustomLocalizationResolver(localizationService, guildSettingsService, bundle.value, bundle.prefix.nullIfBlank())
 *     }
 * }
 * ```
 *
 * @param priority Priority of this resolver factory, see [ParameterResolverFactory.priority]
 * @param producer Function providing a [resolver][ParameterResolver] for the provided function parameter
 * @param R Type of the object returned by the resolver
 *
 * @see ParameterResolverFactory
 * @see ParameterResolver
 */
inline fun <reified T : ParameterResolver<T, R>, reified R : Any> resolverFactory(priority: Int = 0, crossinline producer: (request: ResolverRequest) -> T): ParameterResolverFactory {
    val currentFrame = currentFrame()
    val declarationSiteSignature =
        "${currentFrame.declaringClass.shortQualifiedName}.${currentFrame.methodName.substringBefore('$')} (${currentFrame.sourceFile}:${currentFrame.lineNumber})"
    return object : TypedParameterResolverFactory(R::class) {
        override val priority: Int get() = priority

        override val supportedResolvers = inferSupportedResolversFrom<T>()

        override fun get(request: ResolverRequest): IParameterResolver<*> = producer(request)

        override fun toLogString(): String = "$declarationSiteSignature ; priority $priority (${supportedTypesStr.single()})"
    }
}

inline fun <reified R : Any> resolverFactory(supportedResolvers: List<Class<out IParameterResolver<*>>>, priority: Int = 0, crossinline producer: (request: ResolverRequest) -> IParameterResolver<*>): ParameterResolverFactory {
    val currentFrame = currentFrame()
    val declarationSiteSignature =
        "${currentFrame.declaringClass.shortQualifiedName}.${currentFrame.methodName.substringBefore('$')} (${currentFrame.sourceFile}:${currentFrame.lineNumber})"
    return object : TypedParameterResolverFactory(R::class) {
        override val priority: Int get() = priority

        override val supportedResolvers = supportedResolvers

        override fun get(request: ResolverRequest): IParameterResolver<*> = producer(request)

        override fun toLogString(): String = "$declarationSiteSignature ; priority $priority (${supportedTypesStr.single()})"
    }
}

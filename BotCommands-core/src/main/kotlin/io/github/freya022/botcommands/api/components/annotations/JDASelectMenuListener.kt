package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.api.components.event.StringSelectEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import kotlin.reflect.KFunction

/**
 * Declares this function as a select menu listener with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 * - The annotation value to have same name as the one given to [IPersistentActionableComponent.bindTo], however,
 * it can be omitted if you use the type-safe [bindWith] extensions.
 * - First parameter must be [StringSelectEvent]/[EntitySelectEvent].
 *
 * ### Option types
 * - User data: Uses [@ComponentData][ComponentData], the order must match the data passed when creating the select menu,
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [ComponentParameterResolver].
 * - [AppLocalizationContext]: Uses [@LocalizationBundle][LocalizationBundle].
 * - Custom options: No annotation, additional types can be added by implementing [ICustomResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * ### Type-safe bindings in Kotlin
 * You can use the [bindWith] extensions to safely pass data,
 * in this case you don't need to set the listener name:
 * ```kt
 * @Command
 * class SlashTypeSafeSelectMenus(private val selectMenus: SelectMenus) : ApplicationCommand() {
 *     @JDASlashCommand(name = "type_safe_select_menus", description = "Demo of Kotlin type-safe bindings")
 *     suspend fun onSlashTypeSafeSelectMenus(event: GuildSlashEvent, @SlashOption argument: String) {
 *         val selectMenu = selectMenus.entitySelectMenu(EntitySelectMenu.SelectTarget.ROLE).persistent {
 *             bindTo(::onTestSelect, argument)
 *         }
 *
 *         event.replyComponents(selectMenu.into()).await()
 *     }
 *
 *     @JDASelectMenuListener // No need for a name if you use the type-safe bindTo extensions
 *     suspend fun onTestSelect(event: EntitySelectEvent, @ComponentData argument: String) {
 *         event.reply_("The argument was: $argument", ephemeral = true).await()
 *     }
 * }
 * ```
 *
 * @see Components
 * @see Aggregate @Aggregate
 *
 * @see Cooldown @Cooldown
 * @see RateLimit @RateLimit
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class JDASelectMenuListener(
    /**
     * Name of the select menu listener, referenced by [IPersistentActionableComponent.bindTo].
     *
     * This can be omitted if you use the type-safe [bindWith] extensions.
     *
     * Defaults to `FullyQualifiedClassName.methodName`.
     */
    @get:JvmName("value") val name: String = ""
)

internal fun JDASelectMenuListener.getEffectiveName(func: KFunction<*>): String {
    return name.ifBlank { "${func.declaringClass.qualifiedName}.${func.name}" }
}
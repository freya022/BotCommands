package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.Components.Companion.defaultTimeout
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.api.components.builder.button.EphemeralButtonBuilder
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.builder.group.EphemeralComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.group.PersistentComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.reference
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import javax.annotation.CheckReturnValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

/**
 * This class lets you create smart components such as buttons, select menus, and groups.
 *
 * Every component can either be persistent or ephemeral, all components can be configured to:
 *  - Be used once
 *  - Have timeouts, [a default timeout][defaultTimeout] is set **on ephemeral components**,
 *  which can be overridden, or set by the `timeout` methods.
 *  - Have handlers
 *  - Have constraints (checks before the button can be used)
 *
 * Except component groups which can only have their timeout configured,
 * their default timeouts are the same as components.
 *
 * ### Persistent components
 *  - Kept after restart
 *  - Handlers are methods; they can have arguments passed to them
 *  - Timeouts are also methods, additionally, they will be rescheduled when the bot restarts
 *
 * ### Ephemeral components
 *  - Are deleted once the bot restarts
 *  - Handlers are closures, they can capture objects, but you [shouldn't capture JDA entities](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)
 *  - Timeouts are also closures, but are not rescheduled when restarting
 *
 * ### Component groups
 *  - If deleted, all contained components are deleted
 *  - If one of the contained components is deleted, then all of its subsequent groups are also deleted
 *
 * **Note:** Component groups cannot contain components with timeouts,
 * you will need to [disable the timeout on the components][ITimeoutableComponent.noTimeout].
 *
 * @see Buttons
 * @see SelectMenus
 */
@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
@BService
@ConditionalService(Components.InstantiationChecker::class)
class Components internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    // -------------------- Persistent groups --------------------

    @Deprecated("Use group + persistent instead", replaceWith = ReplaceWith("group(*components).persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentGroup(vararg components: IdentifiableComponent): PersistentComponentGroupBuilder =
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever())

    @Deprecated("Use group + persistent instead", replaceWith = ReplaceWith("group(*components).persistent { \nblock() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun persistentGroup(vararg components: IdentifiableComponent, block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        persistentGroup(*components).apply(block).buildSuspend()

    // -------------------- Ephemeral groups --------------------

    @Deprecated("Use group + ephemeral instead", replaceWith = ReplaceWith("group(*components).ephemeral()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralGroup(vararg components: IdentifiableComponent): EphemeralComponentGroupBuilder =
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever())

    @Deprecated("Use group + ephemeral instead", replaceWith = ReplaceWith("group(*components).ephemeral { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun ephemeralGroup(vararg components: IdentifiableComponent, block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        ephemeralGroup(*components).apply(block).buildSuspend()

    // -------------------- Persistent buttons --------------------

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(style, label, emoji).persistent()"))
    @JvmOverloads
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        PersistentButtonBuilder(componentController, style, label, emoji, InstanceRetriever())
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(style, label, emoji).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, label, emoji).apply(block).buildSuspend()

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentButton(content: ButtonContent) =
        persistentButton(content.style, content.label, content.emoji)
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun persistentButton(content: ButtonContent, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(content.style, content.label, content.emoji, block)

    // -------------------- Ephemeral buttons --------------------

    @Deprecated("Use button + ephemeral instead", replaceWith = ReplaceWith("button(style, label, emoji).ephemeral()"))
    @JvmOverloads
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        EphemeralButtonBuilder(componentController, style, label, emoji, InstanceRetriever())
    @Deprecated("Use button + ephemeral instead", replaceWith = ReplaceWith("button(style, label, emoji).ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, label, emoji).apply(block).buildSuspend()

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralButton(content: ButtonContent) =
        ephemeralButton(content.style, content.label, content.emoji)
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun ephemeralButton( content: ButtonContent, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(content.style, content.label, content.emoji, block)

    // -------------------- Persistent select menus --------------------

    @Deprecated("Use stringSelectMenu + persistent instead", replaceWith = ReplaceWith("stringSelectMenu().persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentStringSelectMenu() =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())
    @Deprecated("Use stringSelectMenu + persistent instead", replaceWith = ReplaceWith("stringSelectMenu().persistent { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun persistentStringSelectMenu(block: PersistentStringSelectBuilder.() -> Unit) =
        persistentStringSelectMenu().apply(block).buildSuspend()

    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(target).persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentEntitySelectMenu(target: SelectTarget) =
        persistentEntitySelectMenu(enumSetOf(target))
    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(target).persistent { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun persistentEntitySelectMenu(target: SelectTarget, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(enumSetOf(target), block)

    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(targets).persistent()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun persistentEntitySelectMenu(targets: Collection<SelectTarget>) =
        PersistentEntitySelectBuilder(componentController, targets, InstanceRetriever())
    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(targets).persistent { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun persistentEntitySelectMenu(targets: Collection<SelectTarget>, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(targets).apply(block).buildSuspend()

    // -------------------- Ephemeral select menus --------------------

    @Deprecated("Use stringSelectMenu + ephemeral instead", replaceWith = ReplaceWith("stringSelectMenu().ephemeral()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralStringSelectMenu() =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())
    @Deprecated("Use stringSelectMenu + ephemeral instead", replaceWith = ReplaceWith("stringSelectMenu().ephemeral { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun ephemeralStringSelectMenu(block: EphemeralStringSelectBuilder.() -> Unit) =
        ephemeralStringSelectMenu().apply(block).buildSuspend()

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(target).ephemeral()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralEntitySelectMenu(target: SelectTarget) =
        ephemeralEntitySelectMenu(enumSetOf(target))
    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(target).ephemeral { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun ephemeralEntitySelectMenu(target: SelectTarget, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(enumSetOf(target), block)

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(targets).ephemeral()"))
    @CheckReturnValue
    @ScheduledForRemoval
    fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>) =
        EphemeralEntitySelectBuilder(componentController, targets, InstanceRetriever())

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(targets).ephemeral { block() }"))
    @JvmSynthetic
    @ScheduledForRemoval
    suspend inline fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(targets).apply(block).buildSuspend()

    companion object {
        /**
         * The default timeout for components and component groups.
         *
         * Non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmSynthetic
        var defaultTimeout: Duration = 15.minutes

        @JvmStatic
        fun getDefaultTimeout(): JavaDuration = defaultTimeout.toJavaDuration()

        /**
         * Sets the default timeout for components and component groups.
         *
         * Non-positive and infinite durations are considered as a disabled timeout.
         */
        @JvmStatic
        fun setDefaultTimeout(defaultTimeout: JavaDuration) {
            this.defaultTimeout = defaultTimeout.toKotlinDuration()
        }
    }

    internal object InstantiationChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
            if (context.componentsConfig.useComponents) {
                return null
            }

            return "Components needs to be enabled, see ${BComponentsConfig::useComponents.reference}"
        }
    }
}
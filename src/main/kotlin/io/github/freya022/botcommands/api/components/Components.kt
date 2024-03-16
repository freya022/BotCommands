package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.Components.Companion.defaultTimeout
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.api.components.builder.button.ButtonFactory
import io.github.freya022.botcommands.api.components.builder.button.EphemeralButtonBuilder
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupFactory
import io.github.freya022.botcommands.api.components.builder.group.EphemeralComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.group.PersistentComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.select.EntitySelectMenuFactory
import io.github.freya022.botcommands.api.components.builder.select.StringSelectMenuFactory
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
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import java.util.*
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
 *  - Have timeouts, [a default timeout][defaultTimeout] is set,
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
 * ### Java example
 * ```java
 * @Command
 * public class SlashSayAgain extends ApplicationCommand {
 *     private static final String SAY_SENTENCE_HANDLER_NAME = "SlashSayAgain: saySentenceButton";
 *
 *     @JDASlashCommand(name = "say_again", description = "Sends a button to send a message again")
 *     public void onSlashSayAgain(
 *             GuildSlashEvent event,
 *             @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
 *             Components componentsService
 *     ) {
 *         // A button that always works, even after a restart
 *         final var persistentSaySentenceButton = componentsService.persistentButton(ButtonStyle.SECONDARY, "Say '" + sentence + "'")
 *                 // Make sure only the caller can use the button
 *                 .addUsers(event.getUser())
 *                 // The method annotated with a JDAButtonListener of the same name will get called,
 *                 // with the sentence as the argument
 *                 .bindTo(SAY_SENTENCE_HANDLER_NAME, sentence)
 *                 .build();
 *
 *         // A button that gets deleted after restart, here it gets deleted after a timeout of 10 seconds
 *         AtomicReference<Button> temporaryButtonRef = new AtomicReference<>();
 *         final var temporarySaySentenceButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Say '" + sentence + "'")
 *                 // Make sure only the caller can use the button
 *                 .addUsers(event.getUser())
 *                 // The code to run when the button gets clicked
 *                 .bindTo(buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).queue())
 *                 // Disables this button after 10 seconds
 *                 .timeout(Duration.ofSeconds(10), () -> {
 *                     final var newRow = ActionRow.of(persistentSaySentenceButton, temporaryButtonRef.get().asDisabled());
 *                     event.getHook().editOriginalComponents(newRow).queue();
 *                 })
 *                 .build();
 *         temporaryButtonRef.set(temporarySaySentenceButton); // We have to do this to get the button in our timeout handler
 *
 *         event.reply("The first button always works, and the second button gets disabled after 10 seconds")
 *                 .addActionRow(persistentSaySentenceButton, temporarySaySentenceButton)
 *                 .queue();
 *     }
 *
 *     @JDAButtonListener(SAY_SENTENCE_HANDLER_NAME)
 *     public void onSaySentenceClick(ButtonEvent event, String sentence) {
 *         event.reply(sentence).setEphemeral(true).queue();
 *     }
 * }
 * ```
 *
 * ### Kotlin example
 * ```kt
 * @Command
 * class SlashSayAgain : ApplicationCommand() {
 *     @JDASlashCommand(name = "say_again", description = "Sends a button to send a message again")
 *     suspend fun onSlashSayAgain(
 *         event: GuildSlashEvent,
 *         @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) sentence: String,
 *         componentsService: Components
 *     ) {
 *         // A button that always works, even after a restart
 *         val persistentSaySentenceButton = componentsService.persistentButton(ButtonStyle.SECONDARY, "Say '$sentence'") {
 *             // Make sure only the caller can use the button
 *             constraints += event.user
 *
 *             // In Kotlin, you can use callable references,
 *             // which enables you to use persistent callbacks in a type-safe manner
 *             bindTo(::onSaySentenceClick, sentence)
 *         }
 *
 *         // A button that gets deleted after restart, here it gets deleted after a timeout of 10 seconds
 *         // We have to use lateinit as the button is used in a callback
 *         lateinit var temporarySaySentenceButton: Button
 *         temporarySaySentenceButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Say '$sentence'") {
 *             // The code to run when the button gets clicked
 *             bindTo { buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).await() }
 *
 *             // Disables this button after 10 seconds
 *             timeout(10.seconds) {
 *                 val newRow = ActionRow.of(persistentSaySentenceButton, temporarySaySentenceButton.asDisabled())
 *                 event.hook.editOriginalComponents(newRow).await() // Coroutines!
 *             }
 *         }
 *
 *         event.reply("The first button always works, and the second button gets disabled after 10 seconds")
 *             .addActionRow(persistentSaySentenceButton, temporarySaySentenceButton)
 *             .queue()
 *     }
 *
 *     @JDAButtonListener("SlashSayAgain: saySentenceButton")
 *     suspend fun onSaySentenceClick(event: ButtonEvent, sentence: String) {
 *         event.reply(sentence).setEphemeral(true).await()
 *     }
 * }
 * ```
 */
@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
@BService
@ConditionalService(Components.InstantiationChecker::class)
class Components internal constructor(private val componentController: ComponentController) {
    private val logger = KotlinLogging.logger { }

    // -------------------- Persistent groups --------------------

    @Deprecated("Use group + persistent instead", replaceWith = ReplaceWith("group(*components).persistent()"))
    @CheckReturnValue
    fun persistentGroup(vararg components: IdentifiableComponent): PersistentComponentGroupBuilder =
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever())

    @Deprecated("Use group + persistent instead", replaceWith = ReplaceWith("group(*components).persistent { \nblock() }"))
    @JvmSynthetic
    suspend inline fun persistentGroup(vararg components: IdentifiableComponent, block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        persistentGroup(*components).apply(block).buildSuspend()

    // -------------------- Ephemeral groups --------------------

    @Deprecated("Use group + ephemeral instead", replaceWith = ReplaceWith("group(*components).ephemeral()"))
    @CheckReturnValue
    fun ephemeralGroup(vararg components: IdentifiableComponent): EphemeralComponentGroupBuilder =
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever())

    @Deprecated("Use group + ephemeral instead", replaceWith = ReplaceWith("group(*components).ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralGroup(vararg components: IdentifiableComponent, block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        ephemeralGroup(*components).apply(block).buildSuspend()

    // -------------------- Persistent buttons --------------------

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(style, label, emoji).persistent()"))
    @JvmOverloads
    @CheckReturnValue
    fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        PersistentButtonBuilder(componentController, style, label, emoji, InstanceRetriever())
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(style, label, emoji).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, label, emoji).apply(block).buildSuspend()

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent()"))
    @CheckReturnValue
    fun persistentButton(content: ButtonContent) =
        persistentButton(content.style, content.label, content.emoji)
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentButton(content: ButtonContent, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(content.style, content.label, content.emoji, block)

    // -------------------- Ephemeral buttons --------------------

    @Deprecated("Use button + ephemeral instead", replaceWith = ReplaceWith("button(style, label, emoji).ephemeral()"))
    @JvmOverloads
    @CheckReturnValue
    fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        EphemeralButtonBuilder(componentController, style, label, emoji, InstanceRetriever())
    @Deprecated("Use button + ephemeral instead", replaceWith = ReplaceWith("button(style, label, emoji).ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, label, emoji).apply(block).buildSuspend()

    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent()"))
    @CheckReturnValue
    fun ephemeralButton(content: ButtonContent) =
        ephemeralButton(content.style, content.label, content.emoji)
    @Deprecated("Use button + persistent instead", replaceWith = ReplaceWith("button(content).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralButton( content: ButtonContent, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(content.style, content.label, content.emoji, block)

    // -------------------- Persistent select menus --------------------

    @Deprecated("Use stringSelectMenu + persistent instead", replaceWith = ReplaceWith("stringSelectMenu().persistent()"))
    @CheckReturnValue
    fun persistentStringSelectMenu() =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())
    @Deprecated("Use stringSelectMenu + persistent instead", replaceWith = ReplaceWith("stringSelectMenu().persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentStringSelectMenu(block: PersistentStringSelectBuilder.() -> Unit) =
        persistentStringSelectMenu().apply(block).buildSuspend()

    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(target).persistent()"))
    @CheckReturnValue
    fun persistentEntitySelectMenu(target: SelectTarget) =
        persistentEntitySelectMenu(enumSetOf(target))
    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(target).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentEntitySelectMenu(target: SelectTarget, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(enumSetOf(target), block)

    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(targets).persistent()"))
    @CheckReturnValue
    fun persistentEntitySelectMenu(targets: Collection<SelectTarget>) =
        PersistentEntitySelectBuilder(componentController, targets, InstanceRetriever())
    @Deprecated("Use entitySelectMenu + persistent instead", replaceWith = ReplaceWith("entitySelectMenu(targets).persistent { block() }"))
    @JvmSynthetic
    suspend inline fun persistentEntitySelectMenu(targets: Collection<SelectTarget>, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(targets).apply(block).buildSuspend()

    // -------------------- Ephemeral select menus --------------------

    @Deprecated("Use stringSelectMenu + ephemeral instead", replaceWith = ReplaceWith("stringSelectMenu().ephemeral()"))
    @CheckReturnValue
    fun ephemeralStringSelectMenu() =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())
    @Deprecated("Use stringSelectMenu + ephemeral instead", replaceWith = ReplaceWith("stringSelectMenu().ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralStringSelectMenu(block: EphemeralStringSelectBuilder.() -> Unit) =
        ephemeralStringSelectMenu().apply(block).buildSuspend()

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(target).ephemeral()"))
    @CheckReturnValue
    fun ephemeralEntitySelectMenu(target: SelectTarget) =
        ephemeralEntitySelectMenu(enumSetOf(target))
    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(target).ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralEntitySelectMenu(target: SelectTarget, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(enumSetOf(target), block)

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(targets).ephemeral()"))
    @CheckReturnValue
    fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>) =
        EphemeralEntitySelectBuilder(componentController, targets, InstanceRetriever())

    @Deprecated("Use entitySelectMenu + ephemeral instead", replaceWith = ReplaceWith("entitySelectMenu(targets).ephemeral { block() }"))
    @JvmSynthetic
    suspend inline fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(targets).apply(block).buildSuspend()

    // -------------------- Groups --------------------

    @CheckReturnValue
    fun group(vararg components: IdentifiableComponent): ComponentGroupFactory =
        ComponentGroupFactory(componentController, components)

    // -------------------- Buttons --------------------

    /**
     * Creates a button factory with the style and label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is blank
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun button(style: ButtonStyle, label: String): ButtonFactory =
        ButtonFactory(componentController, style, label, null)

    /**
     * Creates a button factory with the style and emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun button(style: ButtonStyle, emoji: Emoji): ButtonFactory =
        ButtonFactory(componentController, style, null, emoji)

    /**
     * Creates a button factory with the style, label and emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is blank
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun button(style: ButtonStyle, label: String, emoji: Emoji): ButtonFactory =
        ButtonFactory(componentController, style, label, emoji)

    /**
     * Creates a button factory with the style, label and emoji provided by the [ButtonContent].
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is null/blank and the emoji isn't set
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonContent.withEmoji
     */
    @CheckReturnValue
    fun button(content: ButtonContent): ButtonFactory =
        ButtonFactory(componentController, content.style, content.label, content.emoji)

    // -------------------- Select menus --------------------

    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @CheckReturnValue
    fun stringSelectMenu(): StringSelectMenuFactory = StringSelectMenuFactory(componentController)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @CheckReturnValue
    fun entitySelectMenu(target: SelectTarget, vararg targets: SelectTarget): EntitySelectMenuFactory =
        entitySelectMenu(EnumSet.of(target, *targets))

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @CheckReturnValue
    fun entitySelectMenu(targets: Collection<SelectTarget>): EntitySelectMenuFactory =
        EntitySelectMenuFactory(componentController, targets)

    @JvmName("deleteComponentsById")
    fun deleteComponentsByIdJava(ids: Collection<String>) = runBlocking { deleteComponentsById(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsById(ids: Collection<String>) {
        val parsedIds = ids
            .filter {
                if (ComponentController.isCompatibleComponent(it)) {
                    true
                } else {
                    logger.warn { "Tried to delete an incompatible component ID '$it'" }
                    false
                }
            }
            .map { ComponentController.parseComponentId(it) }

        componentController.deleteComponentsById(parsedIds, throwTimeouts = false)
    }

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

//typealias Components = Components2
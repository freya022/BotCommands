package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.button.EphemeralButtonBuilder
import io.github.freya022.botcommands.api.components.builder.button.PersistentButtonBuilder
import io.github.freya022.botcommands.api.components.builder.group.EphemeralComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.group.PersistentComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import io.github.freya022.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.utils.ButtonContent
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget

/**
 * This class lets you create smart components such as buttons, select menus, and groups.
 *
 * Every component can either be persistent or ephemeral, all components can be configured to:
 *  - Be used once
 *  - Have timeouts
 *  - Have handlers
 *  - Have constraints (checks before the button can be used)
 *
 * Except component groups which can only have its timeout configured.
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
@Suppress("MemberVisibilityCanBePrivate")
@BService
@ConditionalService(Components.Companion::class)
class Components internal constructor(private val componentController: ComponentController) {
    private val logger = KotlinLogging.logger { }

    // -------------------- Persistent groups --------------------

    fun persistentGroup(vararg components: IdentifiableComponent): PersistentComponentGroupBuilder =
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun persistentGroup(vararg components: IdentifiableComponent, block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        persistentGroup(*components).apply(block).buildSuspend()

    // -------------------- Ephemeral groups --------------------

    fun ephemeralGroup(vararg components: IdentifiableComponent): EphemeralComponentGroupBuilder =
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever())

    @JvmSynthetic
    suspend inline fun ephemeralGroup(vararg components: IdentifiableComponent, block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        ephemeralGroup(*components).apply(block).buildSuspend()

    // -------------------- Persistent buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        PersistentButtonBuilder(style, componentController, label, emoji, InstanceRetriever())
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    suspend inline fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, label, emoji).apply(block).buildSuspend()

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun persistentButton(style: ButtonStyle, content: ButtonContent) =
        persistentButton(style, content.text, content.emoji)
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    suspend inline fun persistentButton(style: ButtonStyle, content: ButtonContent, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, content.text, content.emoji, block)

    // -------------------- Ephemeral buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        EphemeralButtonBuilder(style, componentController, label, emoji, InstanceRetriever())
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    suspend inline fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, label, emoji).apply(block).buildSuspend()

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun ephemeralButton(style: ButtonStyle, content: ButtonContent) =
        ephemeralButton(style, content.text, content.emoji)
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    suspend inline fun ephemeralButton(style: ButtonStyle, content: ButtonContent, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, content.text, content.emoji, block)

    // -------------------- Persistent select menus --------------------

    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    fun persistentStringSelectMenu() =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())
    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @JvmSynthetic
    suspend inline fun persistentStringSelectMenu(block: PersistentStringSelectBuilder.() -> Unit) =
        persistentStringSelectMenu().apply(block).buildSuspend()

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun persistentEntitySelectMenu(target: SelectTarget) =
        persistentEntitySelectMenu(enumSetOf(target))
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    suspend inline fun persistentEntitySelectMenu(target: SelectTarget, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(enumSetOf(target), block)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun persistentEntitySelectMenu(targets: Collection<SelectTarget>) =
        PersistentEntitySelectBuilder(componentController, targets, InstanceRetriever())
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    suspend inline fun persistentEntitySelectMenu(targets: Collection<SelectTarget>, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(targets).apply(block).buildSuspend()

    // -------------------- Ephemeral select menus --------------------

    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    fun ephemeralStringSelectMenu() =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())
    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @JvmSynthetic
    suspend inline fun ephemeralStringSelectMenu(block: EphemeralStringSelectBuilder.() -> Unit) =
        ephemeralStringSelectMenu().apply(block).buildSuspend()

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun ephemeralEntitySelectMenu(target: SelectTarget) =
        ephemeralEntitySelectMenu(enumSetOf(target))
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    suspend inline fun ephemeralEntitySelectMenu(target: SelectTarget, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(enumSetOf(target), block)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>) =
        EphemeralEntitySelectBuilder(componentController, targets, InstanceRetriever())
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    suspend inline fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(targets).apply(block).buildSuspend()

    @JvmName("deleteComponentsById")
    fun deleteComponentsByIdJava(ids: Collection<String>) = runBlocking { deleteComponentsById(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsById(ids: Collection<String>) {
        componentController.deleteComponentsById(ids.mapNotNull { it.toIntOrNull() }, throwTimeouts = false)
    }

    internal companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>): String? {
            if (context.componentsConfig.useComponents) {
                return null
            }

            return "Components needs to be enabled, see ${BComponentsConfig::useComponents.reference}"
        }
    }
}
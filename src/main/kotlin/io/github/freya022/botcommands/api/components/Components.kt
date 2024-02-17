package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.ReceiverConsumer
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
 * public class SlashButton extends ApplicationCommand {
 *     private static final String BUTTON_LISTENER_NAME = "SlashButton: persistentButton"; //ClassName: theButtonPurpose
 *
 *     private final Components componentsService;
 *
 *     public SlashButton(Components componentsService) {
 *         this.componentsService = componentsService;
 *     }
 *
 *     @JDASlashCommand(name = "button", description = "Try out the new buttons!")
 *     public void onSlashButton(GuildSlashEvent event) {
 *         final List<Button> components = new ArrayList<>();
 *
 *         components.add(componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Click me under 5 seconds", builder -> {
 *             builder.timeout(5, TimeUnit.SECONDS, () -> {
 *                 event.getHook()
 *                         .editOriginalComponents(ActionRow.of(
 *                                 components.stream().map(Button::asDisabled).collect(Collectors.toList())
 *                         ))
 *                         .queue();
 *             });
 *
 *             builder.bindTo(buttonEvent -> {
 *                 buttonEvent.editButton(buttonEvent.getButton().asDisabled()).queue();
 *             });
 *         }));
 *
 *         components.add(componentsService.persistentButton(ButtonStyle.SECONDARY, "Click me anytime", builder -> {
 *             builder.bindTo(BUTTON_LISTENER_NAME);
 *         }));
 *
 *         event.replyComponents(ActionRow.of(components))
 *                 .setContent(TimeFormat.RELATIVE.format(Instant.now().plus(Duration.ofSeconds(5))))
 *                 .setEphemeral(true)
 *                 .queue();
 *     }
 *
 *     @JDAButtonListener(name = BUTTON_LISTENER_NAME)
 *     public void onPersistentButtonClick(ButtonEvent event) {
 *         event.editButton(event.getButton().asDisabled()).queue();
 *     }
 * }
 * ```
 *
 * ### Kotlin example
 * ```kt
 * private const val buttonListenerName = "SlashButton: persistentButton" //ClassName: theButtonPurpose
 *
 * @Command
 * class SlashButton(private val componentsService: Components) : ApplicationCommand() {
 *     @JDASlashCommand(name = "button", description = "Try out the new buttons!")
 *     fun onSlashButton(event: GuildSlashEvent) {
 *         val components: MutableList<Button> = arrayListOf()
 *         components += componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Click me under 5 seconds") {
 *             timeout(5.seconds) {
 *                 event.hook.editOriginalComponents(components.map(Button::asDisabled).row()).queue()
 *             }
 *             bindTo { buttonEvent ->
 *                 buttonEvent.editButton(buttonEvent.button.asDisabled()).await() // Coroutines!
 *             }
 *         }
 *
 *         components += componentsService.persistentButton(ButtonStyle.SECONDARY, "Click me anytime") {
 *             bindTo(buttonListenerName)
 *         }
 *
 *         event.replyComponents(listOf(components.row()))
 *             .setContent(TimeFormat.RELATIVE.format(Instant.now() + 5.seconds.toJavaDuration()))
 *             .setEphemeral(true)
 *             .queue()
 *     }
 *
 *     @JDAButtonListener(name = buttonListenerName)
 *     suspend fun onPersistentButtonClick(event: ButtonEvent) {
 *         event.editButton(event.button.asDisabled()).await()
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

    fun newPersistentGroup(block: ReceiverConsumer<PersistentComponentGroupBuilder>, vararg components: IdentifiableComponent): ComponentGroup = runBlocking {
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever()).apply(block).build()
    }

    @JvmSynthetic
    suspend fun newPersistentGroup(vararg components: IdentifiableComponent, block: ReceiverConsumer<PersistentComponentGroupBuilder>): ComponentGroup =
        PersistentComponentGroupBuilder(componentController, components, InstanceRetriever()).apply(block).build()

    // -------------------- Ephemeral groups --------------------

    fun newEphemeralGroup(block: ReceiverConsumer<EphemeralComponentGroupBuilder>, vararg components: IdentifiableComponent): ComponentGroup = runBlocking {
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever()).apply(block).build()
    }

    @JvmSynthetic
    suspend fun newEphemeralGroup(vararg components: IdentifiableComponent, block: ReceiverConsumer<EphemeralComponentGroupBuilder>): ComponentGroup =
        EphemeralComponentGroupBuilder(componentController, components, InstanceRetriever()).apply(block).build()

    // -------------------- Persistent buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        PersistentButtonBuilder(style, componentController, label, emoji, InstanceRetriever())
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    inline fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, label, emoji).apply(block).build()

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun persistentButton(style: ButtonStyle, content: ButtonContent) =
        PersistentButtonBuilder(style, componentController, content.text, content.emoji, InstanceRetriever())
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    inline fun persistentButton(style: ButtonStyle, content: ButtonContent, block: PersistentButtonBuilder.() -> Unit) =
        persistentButton(style, content).apply(block).build()

    // -------------------- Ephemeral buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null) =
        EphemeralButtonBuilder(style, componentController, label, emoji, InstanceRetriever())
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    inline fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, label, emoji).apply(block).build()

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun ephemeralButton(style: ButtonStyle, content: ButtonContent) =
        ephemeralButton(style, content.text, content.emoji)
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmSynthetic
    inline fun ephemeralButton(style: ButtonStyle, content: ButtonContent, block: EphemeralButtonBuilder.() -> Unit) =
        ephemeralButton(style, content.text, content.emoji, block)

    // -------------------- Persistent select menus --------------------

    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    fun persistentStringSelectMenu() =
        PersistentStringSelectBuilder(componentController, InstanceRetriever())
    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @JvmSynthetic
    inline fun persistentStringSelectMenu(block: PersistentStringSelectBuilder.() -> Unit) =
        persistentStringSelectMenu().apply(block).build()

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun persistentEntitySelectMenu(target: SelectTarget) =
        persistentEntitySelectMenu(enumSetOf(target))
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    inline fun persistentEntitySelectMenu(target: SelectTarget, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(enumSetOf(target), block)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun persistentEntitySelectMenu(targets: Collection<SelectTarget>) =
        PersistentEntitySelectBuilder(componentController, targets, InstanceRetriever())
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    inline fun persistentEntitySelectMenu(targets: Collection<SelectTarget>, block: PersistentEntitySelectBuilder.() -> Unit) =
        persistentEntitySelectMenu(targets).apply(block).build()

    // -------------------- Ephemeral select menus --------------------

    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    fun ephemeralStringSelectMenu() =
        EphemeralStringSelectBuilder(componentController, InstanceRetriever())
    /** See [StringSelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create] */
    @JvmSynthetic
    inline fun ephemeralStringSelectMenu(block: EphemeralStringSelectBuilder.() -> Unit) =
        ephemeralStringSelectMenu().apply(block).build()

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun ephemeralEntitySelectMenu(target: SelectTarget) =
        ephemeralEntitySelectMenu(enumSetOf(target))
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    inline fun ephemeralEntitySelectMenu(target: SelectTarget, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(enumSetOf(target), block)

    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>) =
        EphemeralEntitySelectBuilder(componentController, targets, InstanceRetriever())
    /** See [EntitySelectMenu.create][net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.create] */
    @JvmSynthetic
    inline fun ephemeralEntitySelectMenu(targets: Collection<SelectTarget>, block: EphemeralEntitySelectBuilder.() -> Unit) =
        ephemeralEntitySelectMenu(targets).apply(block).build()

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
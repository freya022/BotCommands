package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.components.builder.button.EphemeralButtonBuilder
import com.freya02.botcommands.api.components.builder.button.PersistentButtonBuilder
import com.freya02.botcommands.api.components.builder.group.ComponentGroupBuilder
import com.freya02.botcommands.api.components.builder.group.EphemeralComponentGroupBuilder
import com.freya02.botcommands.api.components.builder.group.PersistentComponentGroupBuilder
import com.freya02.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import com.freya02.botcommands.api.components.builder.select.ephemeral.EphemeralStringSelectBuilder
import com.freya02.botcommands.api.components.builder.select.persistent.PersistentEntitySelectBuilder
import com.freya02.botcommands.api.components.builder.select.persistent.PersistentStringSelectBuilder
import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.config.BComponentsConfig
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ConditionalService
import com.freya02.botcommands.api.utils.ButtonContent
import com.freya02.botcommands.internal.components.controller.ComponentController
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.referenceString
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget

//TODO fix docs
/**
 * This class lets you create smart components such as buttons, select menus and groups.
 *
 * Every component can either be persistent or ephemeral, all components can be configured to:
 *  - Be used once
 *  - Have timeouts
 *  - Have handlers
 *  - Have constraints (checks before the button can be used)
 *
 * Except component groups which can only have its timeout configured.
 *
 * **Persistent components**:
 *  - Kept after restart
 *  - Handlers are methods, they can have arguments passed to them
 *  - Timeouts are also methods, additionally they will be rescheduled when the bot restarts
 *
 * **Ephemeral components**:
 *  - Are deleted once the bot restarts
 *  - Handlers are closures, they can capture objects, but you [shouldn't capture JDA entities](https://jda.wiki/using-jda/troubleshooting/#cannot-get-reference-as-it-has-already-been-garbage-collected)
 *  - Timeouts are also closures, but are not rescheduled when restarting
 *
 * **Component groups**:
 *  - If deleted, all contained components are deleted
 *  - If one of the contained components is deleted, then all of its subsequent groups are also deleted
 *
 * **Example**:
 * ```java
 * public class SlashButton extends ApplicationCommand {
 * 	private static final String PERSISTENT_BUTTON_LISTENER_NAME = "leBouton";
 *
 * 	@JDASlashCommand(name = "button")
 * 	public void onSlashButton(GuildSlashEvent event, Components components) {
 * 		event.reply("Buttons")
 * 				.addActionRow(
 * 						components.persistentButton(ButtonStyle.PRIMARY, "Persistent button (1 minute timeout)", builder -> {
 * 							builder.setOneUse(true);
 * 							builder.bindTo(PERSISTENT_BUTTON_LISTENER_NAME, System.currentTimeMillis());
 * 							builder.timeout(1, TimeUnit.MINUTES);
 * 						}),
 * 						components.ephemeralButton(ButtonStyle.PRIMARY, "Ephemeral button (1 second timeout)", builder -> {
 * 							builder.bindTo(btnEvt -> btnEvt.deferEdit().queue());
 * 							builder.timeout(1, TimeUnit.SECONDS, () -> event.getHook().editOriginal("Ephemeral expired :/").queue());
 * 						})
 * 				)
 * 				.setEphemeral(true)
 * 				.queue();
 * 	}
 *
 * 	@JDAButtonListener(name = PERSISTENT_BUTTON_LISTENER_NAME)
 * 	public void onPersistentButtonClicked(ButtonEvent event, @AppOption long timeCreated, JDA jda) {
 * 		event.replyFormat("Button created on %s and I am %s", timeCreated, jda.getSelfUser().getAsTag())
 * 				.setEphemeral(true)
 * 				.queue();
 * 	}
 * }
 * ```
 */
@BService
@ConditionalService([Components.Companion::class])
class Components internal constructor(private val componentController: ComponentController) {
    private val logger = KotlinLogging.logger { }

    // -------------------- Persistent groups --------------------

    fun newPersistentGroup(block: PersistentComponentGroupBuilder.() -> Unit, vararg components: ActionComponent): ComponentGroup = runBlocking {
        createGroup({ PersistentComponentGroupBuilder(it).apply(block) }, *components)
    }

    @JvmSynthetic
    suspend fun newPersistentGroup(vararg components: ActionComponent, block: PersistentComponentGroupBuilder.() -> Unit): ComponentGroup =
        createGroup({ PersistentComponentGroupBuilder(it).apply(block) }, *components)

    // -------------------- Ephemeral groups --------------------

    fun newEphemeralGroup(block: EphemeralComponentGroupBuilder.() -> Unit, vararg components: ActionComponent): ComponentGroup = runBlocking {
        createGroup({ EphemeralComponentGroupBuilder(it).apply(block) }, *components)
    }

    @JvmSynthetic
    suspend fun newEphemeralGroup(vararg components: ActionComponent, block: EphemeralComponentGroupBuilder.() -> Unit): ComponentGroup =
        createGroup({ EphemeralComponentGroupBuilder(it).apply(block) }, *components)

    // -------------------- Persistent buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun persistentButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: ReceiverConsumer<PersistentButtonBuilder>) =
        PersistentButtonBuilder(style, componentController).apply(block).build(label, emoji)
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun persistentButton(style: ButtonStyle, content: ButtonContent, block: ReceiverConsumer<PersistentButtonBuilder>) =
        persistentButton(style, content.text, content.emoji, block)

    // -------------------- Ephemeral buttons --------------------

    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    @JvmOverloads
    fun ephemeralButton(style: ButtonStyle, label: String? = null, emoji: Emoji? = null, block: ReceiverConsumer<EphemeralButtonBuilder>) =
        EphemeralButtonBuilder(style, componentController).apply(block).build(label, emoji)
    /** See [Button.of][net.dv8tion.jda.api.interactions.components.buttons.Button.of] */
    fun ephemeralButton(style: ButtonStyle, content: ButtonContent, block: ReceiverConsumer<EphemeralButtonBuilder>) =
        ephemeralButton(style, content.text, content.emoji, block)

    // -------------------- Persistent select menus --------------------

    fun persistentStringSelectMenu(block: ReceiverConsumer<PersistentStringSelectBuilder>) =
        PersistentStringSelectBuilder(componentController).apply(block).doBuild()
    fun persistentEntitySelectMenu(target: SelectTarget, block: ReceiverConsumer<PersistentEntitySelectBuilder>) =
        persistentEntitySelectMenu(listOf(target), block)
    fun persistentEntitySelectMenu(targets: List<SelectTarget>, block: ReceiverConsumer<PersistentEntitySelectBuilder>) =
        PersistentEntitySelectBuilder(componentController, targets).apply(block).doBuild()

    // -------------------- Ephemeral select menus --------------------

    fun ephemeralStringSelectMenu(block: ReceiverConsumer<EphemeralStringSelectBuilder>) =
        EphemeralStringSelectBuilder(componentController).apply(block).doBuild()
    fun ephemeralEntitySelectMenu(target: SelectTarget, block: ReceiverConsumer<EphemeralEntitySelectBuilder>) =
        ephemeralEntitySelectMenu(listOf(target), block)
    fun ephemeralEntitySelectMenu(targets: List<SelectTarget>, block: ReceiverConsumer<EphemeralEntitySelectBuilder>) =
        EphemeralEntitySelectBuilder(componentController, targets).apply(block).doBuild()

    @JvmName("deleteComponentsById")
    fun deleteComponentsByIdJava(ids: Collection<String>) = runBlocking { deleteComponentsById(ids) }

    @JvmSynthetic
    suspend fun deleteComponentsById(ids: Collection<String>) {
        componentController.deleteComponentsById(ids.mapNotNull { it.toIntOrNull() })
    }

    private suspend fun createGroup(factory: (List<Int>) -> ComponentGroupBuilder, vararg components: ActionComponent): ComponentGroup {
        requireUser(components.none { it.id == null }) {
            "Cannot make groups with link buttons"
        }

        return components
            .map { it.id?.toIntOrNull() ?: throwUser("Cannot put external components in groups") }
            .let { componentIds -> factory(componentIds) }
            .let { componentController.insertGroup(it) }
    }

    internal companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext): String? {
            if (context.componentsConfig.useComponents) {
                return null
            }

            return "Components needs to be enabled, see ${BComponentsConfig::useComponents.referenceString}"
        }
    }
}
package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.builder.button.ButtonFactory
import io.github.freya022.botcommands.api.components.utils.ButtonContent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import javax.annotation.CheckReturnValue

/**
 * Factory for buttons, see [Components] for more details.
 *
 * ## Examples
 * ### Persistent button (Kotlin)
 * ```kt
 * @Command
 * class SlashSayAgainPersistent : ApplicationCommand() {
 *     @JDASlashCommand(name = "say_again", subcommand = "persistent", description = "Sends a button to send a message again")
 *     suspend fun onSlashSayAgain(
 *         event: GuildSlashEvent,
 *         @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) sentence: String,
 *         buttons: Buttons
 *     ) {
 *         // A button that always works, even after a restart
 *         val persistentSaySentenceButton = buttons.secondary("Say '$sentence'").persistent {
 *             // Make sure only the caller can use the button
 *             constraints += event.user
 *
 *             // In Kotlin, you can use callable references,
 *             // which enables you to use persistent callbacks in a type-safe manner
 *             bindTo(::onSaySentenceClick, sentence)
 *         }
 *
 *         event.reply("This button always works")
 *             .addActionRow(persistentSaySentenceButton)
 *             .await()
 *     }
 *
 *     @JDAButtonListener("SlashSayAgainPersistent: saySentenceButton")
 *     suspend fun onSaySentenceClick(event: ButtonEvent, sentence: String) {
 *         event.reply_(sentence, ephemeral = true).await()
 *     }
 * }
 * ```
 *
 * ### Ephemeral button (Kotlin)
 * ```kt
 * @Command
 * class SlashSayAgainEphemeral : ApplicationCommand() {
 *     @JDASlashCommand(name = "say_again", subcommand = "ephemeral", description = "Sends a button to send a message again")
 *     suspend fun onSlashSayAgain(
 *         event: GuildSlashEvent,
 *         @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) sentence: String,
 *         buttons: Buttons
 *     ) {
 *         // A button, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
 *         // We have to use lateinit as the button is used in a callback
 *         lateinit var temporarySaySentenceButton: Button
 *         temporarySaySentenceButton = buttons.primary("Say '$sentence'").ephemeral {
 *             // Make sure only the caller can use the button
 *             constraints += event.user
 *
 *             // The code to run when the button gets clicked
 *             bindTo { buttonEvent -> buttonEvent.reply_(sentence, ephemeral = true).await() }
 *
 *             // Disables this button after 10 seconds
 *             timeout(10.seconds) {
 *                 val newRow = row(temporarySaySentenceButton.asDisabled())
 *                 event.hook.editOriginalComponents(newRow).await() // Coroutines!
 *             }
 *         }
 *
 *         event.reply("This button expires ${TimeFormat.RELATIVE.after(10.seconds)}")
 *             .addActionRow(temporarySaySentenceButton)
 *             .await()
 *     }
 * }
 * ```
 *
 * ### Persistent button (Java)
 * ```java
 * @Command
 * public class SlashSayAgainPersistent extends ApplicationCommand {
 *     private static final String SAY_SENTENCE_HANDLER_NAME = "SlashSayAgainPersistent: saySentenceButton";
 *
 *     @JDASlashCommand(name = "say_again", subcommand = "persistent", description = "Sends a button to send a message again")
 *     public void onSlashSayAgain(
 *             GuildSlashEvent event,
 *             @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
 *             Buttons buttons
 *     ) {
 *         // A button that always works, even after a restart
 *         final var persistentSaySentenceButton = buttons.secondary("Say '" + sentence + "'").persistent()
 *                 // Make sure only the caller can use the button
 *                 .addUsers(event.getUser())
 *                 // The method annotated with a JDAButtonListener of the same name will get called,
 *                 // with the sentence as the argument
 *                 .bindTo(SAY_SENTENCE_HANDLER_NAME, sentence)
 *                 .build();
 *
 *         event.reply("This button always works")
 *                 .addActionRow(persistentSaySentenceButton)
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
 * ### Ephemeral button (Java)
 * ```java
 * @Command
 * public class SlashSayAgainEphemeral extends ApplicationCommand {
 *     @JDASlashCommand(name = "say_again", subcommand = "ephemeral", description = "Sends a button to send a message again")
 *     public void onSlashSayAgain(
 *             GuildSlashEvent event,
 *             @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
 *             Buttons buttons
 *     ) {
 *         // A button, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
 *         AtomicReference<Button> temporaryButtonRef = new AtomicReference<>();
 *         final var temporarySaySentenceButton = buttons.primary("Say '" + sentence + "'").ephemeral()
 *                 // Make sure only the caller can use the button
 *                 .addUsers(event.getUser())
 *                 // The code to run when the button gets clicked
 *                 .bindTo(buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).queue())
 *                 // Disables this button after 10 seconds
 *                 .timeout(Duration.ofSeconds(10), () -> {
 *                     final var newRow = ActionRow.of(temporaryButtonRef.get().asDisabled());
 *                     event.getHook().editOriginalComponents(newRow).queue();
 *                 })
 *                 .build();
 *         temporaryButtonRef.set(temporarySaySentenceButton); // We have to do this to get the button in our timeout handler
 *
 *         event.reply("This button expires " + TimeFormat.RELATIVE.after(Duration.ofSeconds(10)))
 *                 .addActionRow(temporarySaySentenceButton)
 *                 .queue();
 *     }
 * }
 * ```
 *
 * @see Components
 * @see SelectMenus
 */
@BService
@ConditionalService(Components.InstantiationChecker::class)
@RequiresComponents
class Buttons internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    /**
     * Creates a button factory with the style and label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun of(style: ButtonStyle, label: String): ButtonFactory =
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
    fun of(style: ButtonStyle, emoji: Emoji): ButtonFactory =
        ButtonFactory(componentController, style, null, emoji)

    /**
     * Creates a button factory with the style, label and emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun of(style: ButtonStyle, label: String, emoji: Emoji): ButtonFactory =
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
    fun of(content: ButtonContent): ButtonFactory =
        ButtonFactory(componentController, content.style, content.label, content.emoji)

    /**
     * Creates a primary button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(label: String): ButtonFactory =
        of(ButtonStyle.PRIMARY, label)

    /**
     * Creates a primary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.PRIMARY, emoji)

    /**
     * Creates a primary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun primary(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.PRIMARY, label, emoji)

    /**
     * Creates a secondary button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(label: String): ButtonFactory =
        of(ButtonStyle.SECONDARY, label)

    /**
     * Creates a secondary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SECONDARY, emoji)

    /**
     * Creates a secondary button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun secondary(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SECONDARY, label, emoji)

    /**
     * Creates a success button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(label: String): ButtonFactory =
        of(ButtonStyle.SUCCESS, label)

    /**
     * Creates a success button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SUCCESS, emoji)

    /**
     * Creates a success button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun success(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.SUCCESS, label, emoji)

    /**
     * Creates a danger button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(label: String): ButtonFactory =
        of(ButtonStyle.DANGER, label)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(emoji: Emoji): ButtonFactory =
        of(ButtonStyle.DANGER, emoji)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     * @see ButtonFactory.withEmoji
     */
    @CheckReturnValue
    fun danger(label: String, emoji: Emoji): ButtonFactory =
        of(ButtonStyle.DANGER, label, emoji)

    /**
     * Creates a danger button factory with the label provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url/label is empty
     */
    @CheckReturnValue
    fun link(url: String, label: String): Button =
        Button.link(url, label)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     */
    @CheckReturnValue
    fun link(url: String, emoji: Emoji): Button =
        Button.link(url, emoji)

    /**
     * Creates a danger button factory with the emoji provided.
     *
     * You can use [ButtonFactory.persistent] or [ButtonFactory.ephemeral] to then start building a button.
     *
     * @throws IllegalArgumentException If the url/label is empty
     *
     * @see EmojiUtils.resolveJDAEmoji
     */
    @CheckReturnValue
    fun link(url: String, label: String, emoji: Emoji): Button =
        Button.link(url, label).withEmoji(emoji)
}
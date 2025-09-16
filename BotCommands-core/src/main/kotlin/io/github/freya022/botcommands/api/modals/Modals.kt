package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.modals.Modals.Companion.defaultTimeout
import java.time.Duration as JavaDuration
import javax.annotation.CheckReturnValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

/**
 * Methods for modals and modal inputs
 *
 * ### Kotlin example
 * ```kotlin
 * private const val MODAL_NAME = "request role"
 * private const val INPUT_REASON = "reason"
 * private const val INPUT_ROLE = "role"
 * private const val INPUT_DETAILS = "details"
 *
 * @Command
 * class SlashRequestRole(private val modals: Modals) {
 *
 *     @JDASlashCommand(name = "request_role", description = "Request a role")
 *     fun onSlashRequestRole(event: GuildSlashEvent) {
 *         val modal = modals.create("Role Request Form") {
 *             text(
 *                 """
 *                     ### Welcome!
 *                     Please read the following before continuing:
 *                     1. Select the role you wish to get
 *                     2. Select the reason why you want this role
 *                     3. (Optional) Add any detail about your request
 *
 *                     -# Abuse of this system may result in penalties
 *                 """.trimIndent()
 *             )
 *
 *             label("Role") {
 *                 child = EntitySelectMenu(INPUT_ROLE, SelectTarget.ROLE)
 *             }
 *
 *             label("Reason") {
 *                 child = StringSelectMenu(INPUT_REASON) {
 *                     option("It looks cool!", "cool")
 *                     option("I like the color", "color")
 *                     option("I am interested in the relevant discussions", "discussions")
 *                 }
 *             }
 *
 *             label("Details") {
 *                 child = TextInput(INPUT_DETAILS, TextInputStyle.PARAGRAPH, isRequired = false)
 *             }
 *
 *             bindTo(MODAL_NAME)
 *         }
 *
 *         event.replyModal(modal).queue()
 *     }
 *
 *     @ModalHandler(MODAL_NAME)
 *     fun onRequestRoleModal(
 *         event: ModalEvent,
 *         @ModalInput(INPUT_REASON) reason: List<String>,
 *         @ModalInput(INPUT_ROLE) roles: List<Role>,
 *         @ModalInput(INPUT_DETAILS) details: String,
 *     ) {
 *         event.reply("Your request has been submitted!")
 *             .setEphemeral(true)
 *             .queue()
 *     }
 * }
 * ```
 *
 * ### Java example
 * ```java
 * @Command
 * public class SlashRequestRole extends ApplicationCommand {
 *
 *     private static final String MODAL_NAME = "request role";
 *     private static final String INPUT_ROLE = "role";
 *     private static final String INPUT_REASON = "reason";
 *     private static final String INPUT_DETAILS = "details";
 *
 *     private final Modals modals;
 *
 *     public SlashRequestRole(Modals modals) {
 *         this.modals = modals;
 *     }
 *
 *     @JDASlashCommand(name = "request_role", description = "Request a role")
 *     public void onSlashRequestRole(GuildSlashEvent event) {
 *         var modal = modals.create("Role Request Form")
 *                 .addComponents(
 *                         TextDisplay.of("""
 *                                 ### Welcome!
 *                                 Please read the following before continuing:
 *                                 1. Select the role you wish to get
 *                                 2. Select the reason why you want this role
 *                                 3. (Optional) Add any detail about your request
 *
 *                                 -# Abuse of this system may result in penalties
 *                                 """),
 *                         Label.of(
 *                                 "Role",
 *                                 EntitySelectMenu.create(INPUT_ROLE, SelectTarget.ROLE).build()
 *                         ),
 *                         Label.of(
 *                                 "Reason",
 *                                 StringSelectMenu.create(INPUT_REASON)
 *                                         .addOption("It looks cool!", "cool")
 *                                         .addOption("I like the color", "color")
 *                                         .addOption("I am interested in the relevant discussions", "discussions")
 *                                         .build()
 *                         ),
 *                         Label.of(
 *                                 "Details",
 *                                 TextInput.create(INPUT_DETAILS, TextInputStyle.PARAGRAPH)
 *                                         .setRequired(false)
 *                                         .build()
 *                         )
 *                 )
 *                 .bindTo(MODAL_NAME)
 *                 .build();
 *
 *         event.replyModal(modal).queue();
 *     }
 *
 *     @ModalHandler(MODAL_NAME)
 *     public void onRequestRoleModal(
 *             ModalEvent event,
 *             @ModalInput(INPUT_ROLE) List<Role> roles,
 *             @ModalInput(INPUT_REASON) List<String> reason,
 *             @ModalInput(INPUT_DETAILS) String details
 *     ) {
 *         event.reply("Your request has been submitted!")
 *                 .setEphemeral(true)
 *                 .queue();
 *     }
 * }
 * ```
 */
@InterfacedService(acceptMultiple = false)
interface Modals {
    /**
     * Creates a new modal.
     *
     * You can add compatible JDA components in this builder,
     * see [ModalTopLevelComponent][net.dv8tion.jda.api.components.ModalTopLevelComponent].
     *
     * The modal expires after [a default timeout][defaultTimeout],
     * which can be overridden, or set by [ModalBuilder.timeout].
     *
     * @param title The title of the modal
     */
    @CheckReturnValue
    fun create(title: String): ModalBuilder

    companion object {
        @JvmSynthetic
        var defaultTimeout: Duration = 15.minutes

        @JvmStatic
        fun getDefaultTimeout(): JavaDuration = defaultTimeout.toJavaDuration()

        @JvmStatic
        fun setDefaultTimeout(defaultTimeout: JavaDuration) {
            this.defaultTimeout = defaultTimeout.toKotlinDuration()
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Modals.create(title: String, block: InlineModal.() -> Unit): Modal {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return InlineModal(create(title)).apply(block).build()
}

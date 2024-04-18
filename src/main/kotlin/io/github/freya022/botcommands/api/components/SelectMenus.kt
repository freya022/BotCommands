package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.select.EntitySelectMenuFactory
import io.github.freya022.botcommands.api.components.builder.select.StringSelectMenuFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import java.util.*
import javax.annotation.CheckReturnValue

/**
 * Factory for select menus, see [Components] for more details.
 *
 * ## Examples
 * ### Persistent select menus (Kotlin)
 * ```kt
 * @Command
 * class SlashSelectRolePersistent : ApplicationCommand() {
 *     @JDASlashCommand(name = "select_role", subcommand = "persistent", description = "Sends a menu to choose a role from")
 *     suspend fun onSlashSelectRole(event: GuildSlashEvent, selectMenus: SelectMenus) {
 *         val randomNumber = Random.nextLong()
 *         val roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).persistent {
 *             // Make sure only the caller can use the button
 *             constraints += event.user
 *
 *             // In Kotlin, you can use callable references,
 *             // which enables you to use persistent callbacks in a type-safe manner
 *             bindTo(::onRoleMenuSelect, randomNumber)
 *         }
 *
 *         event.reply("This select menu always works")
 *             .addActionRow(roleMenu)
 *             .await()
 *     }
 *
 *     @JDASelectMenuListener("SlashSelectRolePersistent: roleMenu")
 *     suspend fun onRoleMenuSelect(event: EntitySelectEvent, randomNumber: Long) {
 *         val role = event.values[0] as Role
 *         event.reply("You have been given " + role.asMention + ", and the random number is " + randomNumber)
 *             .setEphemeral(true)
 *             .await()
 *     }
 * }
 * ```
 *
 * ### Ephemeral select menus (Kotlin)
 * ```kt
 * @Command
 * class SlashSelectRoleEphemeral : ApplicationCommand() {
 *     @JDASlashCommand(name = "select_role", subcommand = "ephemeral", description = "Sends a menu to choose a role from")
 *     suspend fun onSlashSelectRole(event: GuildSlashEvent, selectMenus: SelectMenus) {
 *         val randomNumber = Random.nextLong()
 *
 *         // A select menu, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
 *         lateinit var temporarySelectMenu: EntitySelectMenu
 *         val roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).ephemeral {
 *             // Make sure only the caller can use the select menu
 *             constraints += event.user
 *
 *             bindTo { selectEvent ->
 *                 val role = selectEvent.values.first() as Role
 *                 selectEvent.reply("You have been given " + role.asMention + ", and the random number is " + randomNumber)
 *                     .setEphemeral(true)
 *                     .await()
 *             }
 *
 *             // Disables this button after 10 seconds
 *             timeout(10.seconds) {
 *                 val newRow = ActionRow.of(temporarySelectMenu.asDisabled())
 *                 event.hook.editOriginalComponents(newRow).await() // Coroutines!
 *             }
 *         }
 *         temporarySelectMenu = roleMenu
 *
 *         event.reply("This select menu expires ${TimeFormat.RELATIVE.after(10.seconds)}")
 *             .addActionRow(roleMenu)
 *             .await()
 *     }
 * }
 * ```
 *
 * ### Persistent select menus (Java)
 * ```java
 * @Command
 * public class SlashSelectRolePersistent extends ApplicationCommand {
 *     private static final String ROLE_MENU_HANDLER_NAME = "SlashSelectRolePersistent: roleMenu";
 *
 *     @JDASlashCommand(name = "select_role", subcommand = "persistent", description = "Sends a menu to choose a role from")
 *     public void onSlashSelectRole(
 *             GuildSlashEvent event,
 *             SelectMenus selectMenus
 *     ) {
 *         final long randomNumber = ThreadLocalRandom.current().nextLong();
 *         final EntitySelectMenu roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).persistent()
 *                 // Make sure only the caller can use the select menu
 *                 .addUsers(event.getUser())
 *                 // The method annotated with a JDASelectMenuListener of the same name will get called,
 *                 // with the random number as the argument
 *                 .bindTo(ROLE_MENU_HANDLER_NAME, randomNumber)
 *                 .build();
 *
 *         event.reply("This select menu always works")
 *                 .addActionRow(roleMenu)
 *                 .queue();
 *     }
 *
 *     @JDASelectMenuListener(ROLE_MENU_HANDLER_NAME)
 *     public void onRoleMenuSelect(EntitySelectEvent event, long randomNumber) {
 *         final Role role = (Role) event.getValues().get(0);
 *         event.reply("You have been given " + role.getAsMention() + ", and the random number is " + randomNumber)
 *                 .setEphemeral(true)
 *                 .queue();
 *     }
 * }
 * ```
 *
 * ### Ephemeral select menus (Java)
 * ```java
 * @Command
 * public class SlashSelectRoleEphemeral extends ApplicationCommand {
 *     @JDASlashCommand(name = "select_role", subcommand = "ephemeral", description = "Sends a menu to choose a role from")
 *     public void onSlashSelectRole(
 *             GuildSlashEvent event,
 *             SelectMenus selectMenus
 *     ) {
 *         final long randomNumber = ThreadLocalRandom.current().nextLong();
 *
 *         // A select menu, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
 *         AtomicReference<EntitySelectMenu> temporarySelectMenuRef = new AtomicReference<>();
 *         final EntitySelectMenu roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).ephemeral()
 *                 // Make sure only the caller can use the select menu
 *                 .addUsers(event.getUser())
 *                 // The code to run when the select menu is used
 *                 .bindTo(selectEvent -> {
 *                     final Role role = (Role) selectEvent.getValues().get(0);
 *                     selectEvent.reply("You have been given " + role.getAsMention() + ", and the random number is " + randomNumber)
 *                             .setEphemeral(true)
 *                             .queue();
 *                 })
 *                 // Disables this button after 10 seconds
 *                 .timeout(Duration.ofSeconds(10), () -> {
 *                     final var newRow = ActionRow.of(temporarySelectMenuRef.get().asDisabled());
 *                     event.getHook().editOriginalComponents(newRow).queue();
 *                 })
 *                 .build();
 *         temporarySelectMenuRef.set(roleMenu);
 *
 *         event.reply("This select menu expires " + TimeFormat.RELATIVE.after(Duration.ofSeconds(10)))
 *                 .addActionRow(roleMenu)
 *                 .queue();
 *     }
 * }
 * ```
 *
 * @see RequiresComponents @RequiresComponents
 * @see Components
 * @see Buttons
 */
@BService
@RequiresComponents
class SelectMenus internal constructor(componentController: ComponentController) : AbstractComponentFactory(componentController) {
    /**
     * Creates a [StringSelectMenu] builder factory.
     *
     * You can use [StringSelectMenuFactory.persistent] or [StringSelectMenuFactory.ephemeral]
     * to then start building a string select menu.
     */
    @CheckReturnValue
    fun stringSelectMenu(): StringSelectMenuFactory = StringSelectMenuFactory(componentController)

    /**
     * Creates a [EntitySelectMenu] builder factory.
     *
     * You can use [EntitySelectMenuFactory.persistent] or [EntitySelectMenuFactory.ephemeral]
     * to then start building an entity select menu.
     */
    @CheckReturnValue
    fun entitySelectMenu(target: SelectTarget, vararg targets: SelectTarget): EntitySelectMenuFactory =
        entitySelectMenu(EnumSet.of(target, *targets))

    /**
     * Creates a [EntitySelectMenu] builder factory.
     *
     * You can use [EntitySelectMenuFactory.persistent] or [EntitySelectMenuFactory.ephemeral]
     * to then start building an entity select menu.
     */
    @CheckReturnValue
    fun entitySelectMenu(targets: Collection<SelectTarget>): EntitySelectMenuFactory =
        EntitySelectMenuFactory(componentController, targets)
}
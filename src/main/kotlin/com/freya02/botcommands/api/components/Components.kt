package com.freya02.botcommands.api.components

import com.freya02.botcommands.annotations.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.annotations.api.components.annotations.JDASelectionMenuListener
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.components.builder.*
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.components.event.SelectionEvent
import com.freya02.botcommands.core.api.annotations.LateService
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import org.jetbrains.annotations.Contract

/**
 * The only class you will have to use to create smart components such as [buttons][Button] and [selection menus][SelectMenu]
 *
 * This class lets you create every type of buttons as well as have builder patterns, while benefiting from the persistent / lambda IDs such as:
 *
 *  - Unlimited argument storage (no more 100 chars limit !)
 *  - One-use components
 *  - Timeouts
 *  - Allowing one or multiple users / roles to interact with them, also define usability by permissions
 *
 * A typical usage could look like this:
 *
 * ```
 * event.reply("Are you sure to ban " + user.getAsMention() + " ?")
 * 	.setEphemeral(true)
 * 	.addActionRow(Components.group(
 * 			Components.dangerButton(BAN_BUTTON_NAME, //Name of the button listener, must be the same as the one given in JDAButtonListener
 * 					callerMember.getIdLong(), //Arguments to pass, they must be mappable to the types of the method "ban" below
 * 					targetMember.getId(),
 * 					delDays,
 * 					"Banned by " + event.getUser().getAsTag() + " : '" + reason + "'").build("Confirm"),
 * 			Components.secondaryButton(CANCEL_BUTTON_NAME, event.getUser().getIdLong()).build("Cancel")
 * 	))
 * 	.queue();
 * ```
 */
@LateService
class Components internal constructor(private val context: BContextImpl, private val componentManager: ComponentManager) {
    private val logger = Logging.getLogger()

    /**
     * Registers the IDs of these components as one group.
     *
     * If one of these components is used, the component and the others from that group will also get deleted.
     *
     * @param components The components to group
     * @return The exact same components for chaining purposes
     */
    fun group(vararg components: ActionComponent): Array<out ActionComponent> {
        componentManager.registerGroup(components.map { it.id })

        return components
    }

    /**
     * Registers the IDs of these components as one group.
     *
     * If one of these components is used, the component and the others from that group will also get deleted.
     *
     * @param components The components to group
     * @return The exact same components for chaining purposes
     */
    fun <T : Collection<ActionComponent>> group(components: T): T {
        componentManager.registerGroup(components.map { it.id })
        return components
    }

    /**
     * Registers the IDs of these ActionRow's components as one group.
     *
     * If one of these components is used, the component and the others from that group will also get deleted.
     *
     * @param rows The ActionRow's components to group
     * @return The exact same components for chaining purposes
     */
    fun groupRows(vararg rows: ActionRow): Array<out ActionRow> {
        componentManager.registerGroup(
            rows
                .flatMap { row: ActionRow -> row.components.filterIsInstance<ActionComponent>() }
                .map { obj: ActionComponent -> obj.id }
        )

        return rows
    }

    /**
     * Registers the IDs of these ActionRow's components as one group.
     *
     * If one of these components is used, the component and the others from that group will also get deleted.
     *
     * @param rows The ActionRow's components to group
     * @return The exact same components for chaining purposes
     */
    fun <T : Collection<ActionRow>> groupRows(rows: T): T {
        componentManager.registerGroup(
            rows
                .flatMap { row: ActionRow -> row.components.filterIsInstance<ActionComponent>() }
                .map { obj: ActionComponent -> obj.id }
        )
        return rows
    }

    /**
     * Applies the supplier [interaction constraints][InteractionConstraints] on these (non-built) components
     *
     * @param constraints The interaction constraints to propagate
     * @param builders    The builders on which the constraints must propagate on
     * @param T           The type of components
     * @return The same components as passed, but with the constraints set
     */
    @Contract("_, _ -> param2")
    fun <T : ComponentBuilder<T>> applyConstraints(
        constraints: InteractionConstraints?,
        vararg builders: T
    ): Array<out T> {
        for (builder in builders) {
            builder.setConstraints(constraints!!)
        }

        return builders
    }

    /**
     * Applies the supplier [interaction constraints][InteractionConstraints] on these (non-built) components
     *
     * @param constraints The interaction constraints to propagate
     * @param builders    The builders on which the constraints must propagate on
     * @param T           The type of components
     * @param C           The type of the collection
     * @return The same components as passed, but with the constraints set
     */
    @Contract("_, _ -> param2")
    fun <T : ComponentBuilder<T>, C : Collection<T>> applyConstraints(
        constraints: InteractionConstraints?,
        builders: C
    ): C {
        for (builder in builders) {
            builder.setConstraints(constraints!!)
        }

        return builders
    }

    /**
     * Creates a new primary button with a lambda [ButtonEvent] handler
     * 
     * **These buttons are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [ButtonEvent] handler, fired after all conditions are met (defined when creating the button)
     * @return A button builder to configure behavior
     */
    @Contract("_ -> new")
    fun primaryButton(consumer: ButtonConsumer): LambdaButtonBuilder {
        checkCapturedVars(consumer)
        return LambdaButtonBuilder(context, consumer, ButtonStyle.PRIMARY)
    }

    /**
     * Creates a new secondary button with a lambda [ButtonEvent] handler
     * 
     * **These buttons are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [ButtonEvent] handler, fired after all conditions are met (defined when creating the button)
     * @return A button builder to configure behavior
     */
    @Contract("_ -> new")
    fun secondaryButton(consumer: ButtonConsumer): LambdaButtonBuilder {
        checkCapturedVars(consumer)
        return LambdaButtonBuilder(context, consumer, ButtonStyle.SECONDARY)
    }

    /**
     * Creates a new danger button with a lambda [ButtonEvent] handler
     * 
     * **These buttons are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [ButtonEvent] handler, fired after all conditions are met (defined when creating the button)
     * @return A button builder to configure behavior
     */
    @Contract("_ -> new")
    fun dangerButton(consumer: ButtonConsumer): LambdaButtonBuilder {
        checkCapturedVars(consumer)
        return LambdaButtonBuilder(context, consumer, ButtonStyle.DANGER)
    }

    /**
     * Creates a new success button with a lambda [ButtonEvent] handler
     * 
     * **These buttons are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [ButtonEvent] handler, fired after all conditions are met (defined when creating the button)
     * @return A button builder to configure behavior
     */
    @Contract("_ -> new")
    fun successButton(consumer: ButtonConsumer): LambdaButtonBuilder {
        checkCapturedVars(consumer)
        return LambdaButtonBuilder(context, consumer, ButtonStyle.SUCCESS)
    }

    /**
     * Creates a new button of the given style, with a lambda [ButtonEvent] handler
     * 
     * **These buttons are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [ButtonEvent] handler, fired after all conditions are met (defined when creating the button)
     * @return A button builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun button(style: ButtonStyle, consumer: ButtonConsumer): LambdaButtonBuilder {
        checkCapturedVars(consumer)
        return LambdaButtonBuilder(context, consumer, style)
    }

    private fun checkCapturedVars(consumer: Any) {
        for (field in consumer.javaClass.declaredFields) {
            if (IMentionable::class.java.isAssignableFrom(field.type)) {
                logger.warn(
                    "A component consumer has a field of type {}, these objects could be invalid when the action is called. Consider having IDs of the objects you need, see https://github.com/DV8FromTheWorld/JDA/wiki/19%29-Troubleshooting#cannot-get-reference-as-it-has-already-been-garbage-collected",
                    field.type
                )
            }
        }
    }

    private fun processArgs(args: Array<out Any>): Array<String> = args.map { arg ->
        if (arg is ISnowflake) {
            arg.id
        } else {
            arg.toString()
        }
    }.toTypedArray()

    /**
     * Creates a new primary button with the given handler name, which must exist as one registered with [JDAButtonListener], and the given arguments
     * 
     * **These buttons *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A button builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun primaryButton(handlerName: String, vararg args: Any): PersistentButtonBuilder {
        return PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.PRIMARY)
    }

    /**
     * Creates a new secondary button with the given handler name, which must exist as one registered with [JDAButtonListener], and the given arguments
     * 
     * **These buttons *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A button builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun secondaryButton(handlerName: String, vararg args: Any): PersistentButtonBuilder {
        return PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SECONDARY)
    }

    /**
     * Creates a new danger button with the given handler name, which must exist as one registered with [JDAButtonListener], and the given arguments
     * 
     * **These buttons *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A button builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun dangerButton(handlerName: String, vararg args: Any): PersistentButtonBuilder {
        return PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.DANGER)
    }

    /**
     * Creates a new success button with the given handler name, which must exist as one registered with [JDAButtonListener], and the given arguments
     * 
     * **These buttons *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A button builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun successButton(handlerName: String, vararg args: Any): PersistentButtonBuilder {
        return PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SUCCESS)
    }

    /**
     * Creates a new button of the given style with the given handler name, which must exist as one registered with [JDAButtonListener], and the given arguments
     * 
     * **These buttons *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A button builder to configure behavior
     */
    @Contract("_, _, _ -> new")
    fun button(style: ButtonStyle, handlerName: String, vararg args: Any): PersistentButtonBuilder {
        return PersistentButtonBuilder(context, handlerName, processArgs(args), style)
    }

    /**
     * Creates a new selection menu with a lambda [SelectionEvent] handler
     * 
     * **These selection menus are not persistent and will not exist anymore once the bot restarts**
     *
     * @param consumer The [SelectionEvent] handler, fired after all conditions are met (defined when creating the selection menu)
     * @return A selection menu builder to configure behavior
     */
    @Contract("_ -> new")
    fun selectionMenu(consumer: SelectionConsumer): LambdaSelectionMenuBuilder {
        checkCapturedVars(consumer)
        return LambdaSelectionMenuBuilder(context, consumer)
    }

    /**
     * Creates a new selection menu with the given handler name, which must exist as one registered with [JDASelectionMenuListener], and the given arguments
     * 
     * **These selection menus *are* persistent and will still exist even if the bot restarts**
     *
     * @param handlerName The name of this component's handler
     * @param args        The args to pass to this component's handler method
     * @return A selection menu builder to configure behavior
     */
    @Contract("_, _ -> new")
    fun selectionMenu(handlerName: String, vararg args: Any): PersistentSelectionMenuBuilder {
        return PersistentSelectionMenuBuilder(context, handlerName, processArgs(args))
    }
}
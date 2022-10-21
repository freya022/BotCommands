package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.builder.*;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The only class you will have to use to create smart components such as {@link Button buttons} and {@link SelectMenu selection menus}<br>
 * This class lets you create every type of buttons as well as have builder patterns, while benefiting from the persistent / lambda IDs such as:
 * <ul>
 *     <li>Unlimited argument storage (no more 100 chars limit !)</li>
 *     <li>One-use components</li>
 *     <li>Timeouts</li>
 *     <li>Allowing one or multiple users / roles to interact with them, also define usability by permissions</li>
 * </ul>
 * A typical usage could look like this:
 *
 * <pre><code>
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
 * </code></pre>
 */
public class Components {
	private static final List<Class<? extends ISnowflake>> RESTRICTED_CLASSES = List.of(Role.class, Channel.class, Guild.class, User.class, Message.class);
	private static final Logger LOGGER = Logging.getLogger();

	private static BContext context;

	static void setContext(BContext context) {
		Components.context = context;
	}

	/**
	 * Registers the IDs of these components as one group.<br>
	 * If one of these components is used, the component and the others from that group will also get deleted.
	 *
	 * @param components The components to group
	 * @return The exact same components for chaining purposes
	 */
	@NotNull
	public static ActionComponent[] group(@NotNull ActionComponent @NotNull ... components) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(components)
						.map(ActionComponent::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	/**
	 * Registers the IDs of these components as one group.<br>
	 * If one of these components is used, the component and the others from that group will also get deleted.
	 *
	 * @param components The components to group
	 * @return The exact same components for chaining purposes
	 */
	@NotNull
	public static <T extends Collection<ActionComponent>> T group(@NotNull T components) {
		Utils.getComponentManager(context).registerGroup(
				components.stream()
						.map(ActionComponent::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	/**
	 * Registers the IDs of these ActionRow's components as one group.<br>
	 * If one of these components is used, the component and the others from that group will also get deleted.
	 *
	 * @param rows The ActionRow's components to group
	 * @return The exact same components for chaining purposes
	 */
	@NotNull
	public static ActionRow[] groupRows(@NotNull ActionRow @NotNull ... rows) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(rows)
						.flatMap(row -> row.getComponents().stream()
								.filter(ActionComponent.class::isInstance) //See ActionRow#getActionComponents
								.map(ActionComponent.class::cast))
						.map(ActionComponent::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	/**
	 * Registers the IDs of these ActionRow's components as one group.<br>
	 * If one of these components is used, the component and the others from that group will also get deleted.
	 *
	 * @param rows The ActionRow's components to group
	 * @return The exact same components for chaining purposes
	 */
	@NotNull
	public static <T extends Collection<ActionRow>> T groupRows(@NotNull T rows) {
		Utils.getComponentManager(context).registerGroup(
				rows.stream()
						.flatMap(row -> row.getComponents().stream()
								.filter(ActionComponent.class::isInstance) //See ActionRow#getActionComponents
								.map(ActionComponent.class::cast))
						.map(ActionComponent::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	/**
	 * Applies the supplier {@link InteractionConstraints interaction constraints} on these (non-built) components
	 *
	 * @param constraints The interaction constraints to propagate
	 * @param builders    The builders on which the constraints must propagate on
	 * @param <T>         The type of components
	 * @return The same components as passed, but with the constraints set
	 */
	@SafeVarargs
	@Contract("_, _ -> param2")
	public static <T extends ComponentBuilder<T>> T[] applyConstraints(InteractionConstraints constraints, @NotNull T @NotNull ... builders) {
		for (T builder : builders) {
			builder.setConstraints(constraints);
		}

		return builders;
	}

	/**
	 * Applies the supplier {@link InteractionConstraints interaction constraints} on these (non-built) components
	 *
	 * @param constraints The interaction constraints to propagate
	 * @param builders    The builders on which the constraints must propagate on
	 * @param <T>         The type of components
	 * @param <C>         The type of the collection
	 * @return The same components as passed, but with the constraints set
	 */
	@Contract("_, _ -> param2")
	public static <T extends ComponentBuilder<T>, C extends Collection<T>> C applyConstraints(InteractionConstraints constraints, @NotNull C builders) {
		for (T builder : builders) {
			builder.setConstraints(constraints);
		}

		return builders;
	}

	/**
	 * Creates a new primary button with a lambda {@link ButtonEvent} handler<br>
	 * <b>These buttons are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link ButtonEvent} handler, fired after all conditions are met (defined when creating the button)
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaButtonBuilder primaryButton(@NotNull ButtonConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, ButtonStyle.PRIMARY);
	}

	/**
	 * Creates a new secondary button with a lambda {@link ButtonEvent} handler<br>
	 * <b>These buttons are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link ButtonEvent} handler, fired after all conditions are met (defined when creating the button)
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaButtonBuilder secondaryButton(@NotNull ButtonConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SECONDARY);
	}

	/**
	 * Creates a new danger button with a lambda {@link ButtonEvent} handler<br>
	 * <b>These buttons are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link ButtonEvent} handler, fired after all conditions are met (defined when creating the button)
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaButtonBuilder dangerButton(@NotNull ButtonConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, ButtonStyle.DANGER);
	}

	/**
	 * Creates a new success button with a lambda {@link ButtonEvent} handler<br>
	 * <b>These buttons are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link ButtonEvent} handler, fired after all conditions are met (defined when creating the button)
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaButtonBuilder successButton(@NotNull ButtonConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SUCCESS);
	}

	/**
	 * Creates a new button of the given style, with a lambda {@link ButtonEvent} handler<br>
	 * <b>These buttons are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link ButtonEvent} handler, fired after all conditions are met (defined when creating the button)
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static LambdaButtonBuilder button(@NotNull ButtonStyle style, @NotNull ButtonConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, style);
	}

	private static void checkCapturedVars(Object consumer) {
		for (Field field : consumer.getClass().getDeclaredFields()) {
			for (Class<?> aClass : RESTRICTED_CLASSES) {
				if (aClass.isAssignableFrom(field.getType())) {
					LOGGER.warn("A component consumer has a field of type {}, these objects could be invalid when the action is called. Consider having IDs of the objects you need, refer to https://github.com/DV8FromTheWorld/JDA/wiki/19%29-Troubleshooting#cannot-get-reference-as-it-has-already-been-garbage-collected", aClass.getSimpleName());
				}
			}
		}
	}

	@NotNull
	private static String[] processArgs(Object[] args) {
		final String[] strings = new String[args.length];

		for (int i = 0, argsLength = args.length; i < argsLength; i++) {
			Object arg = args[i];

			if (arg instanceof ISnowflake) {
				strings[i] = ((ISnowflake) arg).getId();
			} else {
				strings[i] = arg.toString();
			}
		}

		return strings;
	}

	/**
	 * Creates a new primary button with the given handler name, which must exist as one registered with {@link JDAButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder primaryButton(@NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.PRIMARY);
	}

	/**
	 * Creates a new secondary button with the given handler name, which must exist as one registered with {@link JDAButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder secondaryButton(@NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SECONDARY);
	}

	/**
	 * Creates a new danger button with the given handler name, which must exist as one registered with {@link JDAButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder dangerButton(@NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.DANGER);
	}

	/**
	 * Creates a new success button with the given handler name, which must exist as one registered with {@link JDAButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder successButton(@NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SUCCESS);
	}

	/**
	 * Creates a new button of the given style with the given handler name, which must exist as one registered with {@link JDAButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _, _ -> new")
	public static PersistentButtonBuilder button(@NotNull ButtonStyle style, @NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), style);
	}

	/**
	 * Creates a new selection menu with a lambda {@link StringSelectionEvent} handler<br>
	 * <b>These selection menus are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link StringSelectionEvent} handler, fired after all conditions are met (defined when creating the selection menu)
	 * @return A selection menu builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaStringSelectionMenuBuilder selectionMenu(@NotNull StringSelectionConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaStringSelectionMenuBuilder(context, consumer);
	}

	/**
	 * Creates a new selection menu with a lambda {@link StringSelectionEvent} handler<br>
	 * <b>These selection menus are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link StringSelectionEvent} handler, fired after all conditions are met (defined when creating the selection menu)
	 * @return A selection menu builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaEntitySelectionMenuBuilder selectionMenu(@NotNull EntitySelectionConsumer consumer) {
		checkCapturedVars(consumer);

		return new LambdaEntitySelectionMenuBuilder(context, consumer);
	}

	/**
	 * Creates a new selection menu with the given handler name, which must exist as one registered with {@link JDASelectionMenuListener}, and the given arguments<br>
	 * <b>These selection menus <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A selection menu builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentSelectionMenuBuilder selectionMenu(@NotNull String handlerName, @NotNull Object @NotNull ... args) {
		return new PersistentSelectionMenuBuilder(context, handlerName, processArgs(args));
	}
}

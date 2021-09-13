package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.annotations.JdaButtonListener;
import com.freya02.botcommands.api.components.annotations.JdaSelectionMenuListener;
import com.freya02.botcommands.api.components.builder.LambdaButtonBuilder;
import com.freya02.botcommands.api.components.builder.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.PersistentButtonBuilder;
import com.freya02.botcommands.api.components.builder.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.components.event.SelectionEvent;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The only class you will have to use to create smart components such as {@link Button buttons} and {@link SelectionMenu selection menus}<br>
 * This class lets you create every type of buttons as well as have builder patterns, while benefiting from the persistent / lambda IDs such as:
 * <ul>
 *     <li>Unlimited argument storage (no more 100 chars limit !)</li>
 *     <li>One-use components</li>
 *     <li>Timeouts</li>
 *     <li>Allowing one user to interact with them</li>
 * </ul>
 * A typical usage could look like this:
 *
 * <pre><code>
 * event.reply("Are you sure to ban " + user.getAsMention() + " ?")
 * 	.setEphemeral(true)
 * 	.addActionRow(Components.group(
 * 			Components.dangerButton(BAN_BUTTON_NAME, //Name of the button listener, must be the same as the one given in JdaButtonListener
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
	private static final List<Class<? extends ISnowflake>> RESTRICTED_CLASSES = List.of(Role.class, AbstractChannel.class, Guild.class, Emote.class, User.class, Message.class);
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
	public static Component[] group(@NotNull Component... components) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(components)
						.map(Component::getId)
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
	public static <T extends Collection<Component>> T group(@NotNull T components) {
		Utils.getComponentManager(context).registerGroup(
				components.stream()
						.map(Component::getId)
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
	public static ActionRow[] groupRows(@NotNull ActionRow... rows) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(rows)
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
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
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
						.collect(Collectors.toList()));

		return rows;
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
	public static LambdaButtonBuilder primaryButton(@NotNull Consumer<ButtonEvent> consumer) {
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
	public static LambdaButtonBuilder secondaryButton(@NotNull Consumer<ButtonEvent> consumer) {
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
	public static LambdaButtonBuilder dangerButton(@NotNull Consumer<ButtonEvent> consumer) {
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
	public static LambdaButtonBuilder successButton(@NotNull Consumer<ButtonEvent> consumer) {
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
	public static LambdaButtonBuilder button(@NotNull ButtonStyle style, @NotNull Consumer<ButtonEvent> consumer) {
		checkCapturedVars(consumer);

		return new LambdaButtonBuilder(context, consumer, style);
	}

	private static void checkCapturedVars(Consumer<?> consumer) {
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
	 * Creates a new primary button with the given handler name, which must exist as one registered with {@link JdaButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder primaryButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.PRIMARY);
	}

	/**
	 * Creates a new secondary button with the given handler name, which must exist as one registered with {@link JdaButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder secondaryButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SECONDARY);
	}

	/**
	 * Creates a new danger button with the given handler name, which must exist as one registered with {@link JdaButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder dangerButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.DANGER);
	}

	/**
	 * Creates a new success button with the given handler name, which must exist as one registered with {@link JdaButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder successButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SUCCESS);
	}

	/**
	 * Creates a new button of the given style with the given handler name, which must exist as one registered with {@link JdaButtonListener}, and the given arguments<br>
	 * <b>These buttons <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A button builder to configure behavior
	 */
	@NotNull
	@Contract("_, _, _ -> new")
	public static PersistentButtonBuilder button(@NotNull ButtonStyle style, @NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), style);
	}

	/**
	 * Creates a new selection menu with a lambda {@link SelectionEvent} handler<br>
	 * <b>These selection menus are not persistent and will not exist anymore once the bot restarts</b>
	 *
	 * @param consumer The {@link SelectionEvent} handler, fired after all conditions are met (defined when creating the selection menu)
	 * @return A selection menu builder to configure behavior
	 */
	@NotNull
	@Contract("_ -> new")
	public static LambdaSelectionMenuBuilder selectionMenu(@NotNull Consumer<SelectionEvent> consumer) {
		checkCapturedVars(consumer);

		return new LambdaSelectionMenuBuilder(context, consumer);
	}

	/**
	 * Creates a new selection menu with the given handler name, which must exist as one registered with {@link JdaSelectionMenuListener}, and the given arguments<br>
	 * <b>These selection menus <i>are</i> persistent and will still exist even if the bot restarts</b>
	 *
	 * @param handlerName The name of this component's handler
	 * @param args        The args to pass to this component's handler method
	 * @return A selection menu builder to configure behavior
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static PersistentSelectionMenuBuilder selectionMenu(@NotNull String handlerName, Object... args) {
		return new PersistentSelectionMenuBuilder(context, handlerName, processArgs(args));
	}
}

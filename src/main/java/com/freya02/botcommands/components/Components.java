package com.freya02.botcommands.components;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.builder.LambdaButtonBuilder;
import com.freya02.botcommands.components.builder.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.components.builder.PersistentButtonBuilder;
import com.freya02.botcommands.components.builder.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.components.event.SelectionEvent;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Components {
	private static BContext context;

	static void setContext(BContext context) {
		Components.context = context;
	}

	public static Component[] group(Component... components) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(components)
						.map(Component::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	public static <T extends Collection<Component>> T group(T components) {
		Utils.getComponentManager(context).registerGroup(
				components.stream()
						.map(Component::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	public static ActionRow[] groupRows(ActionRow... rows) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(rows)
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	public static <T extends Collection<ActionRow>> T groupRows(T rows) {
		Utils.getComponentManager(context).registerGroup(
				rows.stream()
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	public static LambdaButtonBuilder primaryButton(Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.PRIMARY);
	}

	public static LambdaButtonBuilder secondaryButton(Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SECONDARY);
	}

	public static LambdaButtonBuilder dangerButton(Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.DANGER);
	}

	public static LambdaButtonBuilder successButton(Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SUCCESS);
	}

	public static LambdaButtonBuilder button(ButtonStyle style, Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, style);
	}

	private static List<String> processArgs(Object[] args) {
		final ArrayList<String> strings = new ArrayList<>();

		for (Object arg : args) {
			if (arg instanceof ISnowflake) {
				strings.add(((ISnowflake) arg).getId());
			} else {
				strings.add(arg.toString());
			}
		}

		return strings;
	}

	public static PersistentButtonBuilder primaryButton(String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.PRIMARY);
	}

	public static PersistentButtonBuilder secondaryButton(String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SECONDARY);
	}

	public static PersistentButtonBuilder dangerButton(String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.DANGER);
	}

	public static PersistentButtonBuilder successButton(String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SUCCESS);
	}

	public static PersistentButtonBuilder button(ButtonStyle style, String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), style);
	}

	public static LambdaSelectionMenuBuilder selectionMenu(Consumer<SelectionEvent> consumer) {
		return new LambdaSelectionMenuBuilder(context, consumer);
	}

	public static PersistentSelectionMenuBuilder selectionMenu(String handlerName, Object... args) {
		return new PersistentSelectionMenuBuilder(context, handlerName, processArgs(args));
	}
}

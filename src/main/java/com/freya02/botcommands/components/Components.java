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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Components {
	private static BContext context;

	static void setContext(BContext context) {
		Components.context = context;
	}

	@Nonnull
	public static Component[] group(@NotNull Component... components) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(components)
						.map(Component::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	@Nonnull
	public static <T extends Collection<Component>> T group(@NotNull T components) {
		Utils.getComponentManager(context).registerGroup(
				components.stream()
						.map(Component::getId)
						.collect(Collectors.toList())
		);

		return components;
	}

	@Nonnull
	public static ActionRow[] groupRows(@NotNull ActionRow... rows) {
		Utils.getComponentManager(context).registerGroup(
				Arrays.stream(rows)
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	@Nonnull
	public static <T extends Collection<@NotNull ActionRow>> T groupRows(@NotNull T rows) {
		Utils.getComponentManager(context).registerGroup(
				rows.stream()
						.flatMap(row -> row.getComponents().stream())
						.map(Component::getId)
						.collect(Collectors.toList()));

		return rows;
	}

	@Nonnull
	@Contract("_ -> new")
	public static LambdaButtonBuilder primaryButton(@NotNull Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.PRIMARY);
	}

	@Nonnull
	@Contract("_ -> new")
	public static LambdaButtonBuilder secondaryButton(@NotNull Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SECONDARY);
	}

	@Nonnull
	@Contract("_ -> new")
	public static LambdaButtonBuilder dangerButton(@NotNull Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.DANGER);
	}

	@Nonnull
	@Contract("_ -> new")
	public static LambdaButtonBuilder successButton(@NotNull Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, ButtonStyle.SUCCESS);
	}

	@Nonnull
	@Contract("_, _ -> new")
	public static LambdaButtonBuilder button(@NotNull ButtonStyle style, @NotNull Consumer<ButtonEvent> consumer) {
		return new LambdaButtonBuilder(context, consumer, style);
	}

	@Nonnull
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

	@Nonnull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder primaryButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.PRIMARY);
	}

	@Nonnull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder secondaryButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SECONDARY);
	}

	@Nonnull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder dangerButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.DANGER);
	}

	@Nonnull
	@Contract("_, _ -> new")
	public static PersistentButtonBuilder successButton(@NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), ButtonStyle.SUCCESS);
	}

	@Nonnull
	@Contract("_, _, _ -> new")
	public static PersistentButtonBuilder button(@NotNull ButtonStyle style, @NotNull String handlerName, Object... args) {
		return new PersistentButtonBuilder(context, handlerName, processArgs(args), style);
	}

	@Nonnull
	@Contract("_ -> new")
	public static LambdaSelectionMenuBuilder selectionMenu(@NotNull Consumer<SelectionEvent> consumer) {
		return new LambdaSelectionMenuBuilder(context, consumer);
	}

	@Nonnull
	@Contract("_, _ -> new")
	public static PersistentSelectionMenuBuilder selectionMenu(@NotNull String handlerName, Object... args) {
		return new PersistentSelectionMenuBuilder(context, handlerName, processArgs(args));
	}
}

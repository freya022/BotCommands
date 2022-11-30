package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.builder.buttons.LambdaButtonBuilder;
import com.freya02.botcommands.api.components.builder.buttons.PersistentButtonBuilder;
import com.freya02.botcommands.api.components.builder.selects.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.selects.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.api.components.data.LambdaButtonData;
import com.freya02.botcommands.api.components.data.LambdaSelectionMenuData;
import com.freya02.botcommands.api.components.data.PersistentButtonData;
import com.freya02.botcommands.api.components.data.PersistentSelectionMenuData;
import com.freya02.botcommands.api.core.annotations.InjectedService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The interface which manages components, this goes from creating components, deleting them, or handling their timeouts
 *
 * <p>The default implementation should be used: {@link DefaultComponentManager}
 */
@InjectedService(message = "A component manager strategy needs to be set")
public interface ComponentManager {
	void fetchComponent(String id, Consumer<FetchResult> resultCallback);

	void handleLambdaButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer);

	<E extends GenericSelectMenuInteractionEvent<?, ?>> void handleLambdaSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData<E>> dataConsumer);

	void handlePersistentButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer);

	void handlePersistentSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer);

	@NotNull
	String putLambdaButton(LambdaButtonBuilder builder);

	@NotNull
	<E extends GenericSelectMenuInteractionEvent<?, ?>> String putLambdaSelectMenu(LambdaSelectionMenuBuilder<?, E> builder);

	@NotNull
	String putPersistentButton(PersistentButtonBuilder builder);

	@NotNull
	<T extends PersistentSelectionMenuBuilder<T>> String putPersistentSelectMenu(T builder);

	void registerGroup(Collection<String> ids);

	int deleteIds(Collection<String> ids);

	default int deleteIdsComponents(Collection<ActionComponent> components) {
		final ArrayList<String> ids = new ArrayList<>();

		for (ActionComponent component : components) {
			ids.add(component.getId());
		}

		return deleteIds(ids);
	}

	default int deleteIdRows(Collection<ActionRow> actionRows) {
		final ArrayList<String> ids = new ArrayList<>();

		for (ActionRow actionRow : actionRows) {
			for (ActionComponent component : actionRow.getActionComponents()) {
				ids.add(component.getId());
			}
		}

		return deleteIds(ids);
	}
}

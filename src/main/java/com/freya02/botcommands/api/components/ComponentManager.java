package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.builder.LambdaButtonBuilder;
import com.freya02.botcommands.api.components.builder.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.PersistentButtonBuilder;
import com.freya02.botcommands.api.components.builder.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.api.components.data.LambdaButtonData;
import com.freya02.botcommands.api.components.data.LambdaSelectionMenuData;
import com.freya02.botcommands.api.components.data.PersistentButtonData;
import com.freya02.botcommands.api.components.data.PersistentSelectionMenuData;
import com.freya02.botcommands.api.core.annotations.ConditionalService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
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
@ConditionalService(message = "A component manager strategy needs to be set")
public interface ComponentManager {
	void fetchComponent(String id, Consumer<FetchResult> resultCallback);

	void handleLambdaButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer);

	void handleLambdaSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer);

	void handlePersistentButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer);

	void handlePersistentSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer);

	@NotNull
	String putLambdaButton(LambdaButtonBuilder builder);

	@NotNull
	String putLambdaSelectMenu(LambdaSelectionMenuBuilder builder);

	@NotNull
	String putPersistentButton(PersistentButtonBuilder builder);

	@NotNull
	String putPersistentSelectMenu(PersistentSelectionMenuBuilder builder);

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

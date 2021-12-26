package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.builder.LambdaButtonBuilder;
import com.freya02.botcommands.api.components.builder.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.PersistentButtonBuilder;
import com.freya02.botcommands.api.components.builder.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.internal.components.data.LambdaButtonData;
import com.freya02.botcommands.internal.components.data.LambdaSelectionMenuData;
import com.freya02.botcommands.internal.components.data.PersistentButtonData;
import com.freya02.botcommands.internal.components.data.PersistentSelectionMenuData;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The button id is supposed to be fully treated before returning data, if it is indicated it is one use, the id must be deleted before returning for example
 */
public interface ComponentManager {
	@Nullable
	FetchedComponent fetchComponent(String id);

	void handleLambdaButton(GenericComponentInteractionCreateEvent event, FetchedComponent fetchedComponent, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer);

	void handleLambdaSelectionMenu(GenericComponentInteractionCreateEvent event, FetchedComponent fetchedComponent, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer);

	void handlePersistentButton(GenericComponentInteractionCreateEvent event, FetchedComponent fetchedComponent, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer);

	void handlePersistentSelectionMenu(GenericComponentInteractionCreateEvent event, FetchedComponent fetchedComponent, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer);

	@NotNull
	String putLambdaButton(LambdaButtonBuilder builder);

	@NotNull
	String putLambdaSelectionMenu(LambdaSelectionMenuBuilder builder);

	@NotNull
	String putPersistentButton(PersistentButtonBuilder builder);

	@NotNull
	String putPersistentSelectionMenu(PersistentSelectionMenuBuilder builder);

	void registerGroup(Collection<String> builders);

	int deleteIds(Collection<String> ids);

	default int deleteIdsComponents(Collection<Component> components) {
		final ArrayList<String> ids = new ArrayList<>();

		for (Component component : components) {
			ids.add(component.getId());
		}

		return deleteIds(ids);
	}

	default int deleteIdRows(Collection<ActionRow> actionRows) {
		final ArrayList<String> ids = new ArrayList<>();

		for (ActionRow actionRow : actionRows) {
			for (Component component : actionRow) {
				ids.add(component.getId());
			}
		}

		return deleteIds(ids);
	}
}

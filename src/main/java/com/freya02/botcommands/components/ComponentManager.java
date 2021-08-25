package com.freya02.botcommands.components;

import com.freya02.botcommands.components.builder.LambdaButtonBuilder;
import com.freya02.botcommands.components.builder.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.components.builder.PersistentButtonBuilder;
import com.freya02.botcommands.components.builder.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.components.internal.data.LambdaButtonData;
import com.freya02.botcommands.components.internal.data.LambdaSelectionMenuData;
import com.freya02.botcommands.components.internal.data.PersistentButtonData;
import com.freya02.botcommands.components.internal.data.PersistentSelectionMenuData;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The button id is supposed to be fully treated before returning data, if it is indicated it is one use, the id must be deleted before returning for example
 */
public interface ComponentManager {
	@Nullable
	ComponentType getIdType(String id);

	void handleLambdaButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer);

	void handleLambdaSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer);

	void handlePersistentButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer);

	void handlePersistentSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer);

	@Nonnull
	String putLambdaButton(LambdaButtonBuilder builder);

	@Nonnull
	String putLambdaSelectionMenu(LambdaSelectionMenuBuilder builder);

	@Nonnull
	String putPersistentButton(PersistentButtonBuilder builder);

	@Nonnull
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

package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.InputData;
import com.freya02.botcommands.internal.modals.ModalData;
import com.freya02.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ModalBuilder extends Modal.Builder {
	private final ModalMaps modalMaps;
	private final String handlerName;
	private final Object[] userData;

	@ApiStatus.Internal
	public ModalBuilder(ModalMaps modalMaps, @NotNull String handlerName, Object[] userData) {
		super("0");

		this.modalMaps = modalMaps;
		this.handlerName = handlerName;
		this.userData = userData;
	}

	//TODO timeouts

	//TODO add #addActionRow to avoid ActionRow#of

	@NotNull
	@Override
	@Contract("_ -> fail")
	public Modal.Builder setId(@NotNull String customId) {
		throw new IllegalStateException("Modal ID is already set on this builder");
	}

	@NotNull
	@Override
	public Modal build() {
		final Modal modal = super.build();

		final Map<String, InputData> inputDataMap = new HashMap<>();
		for (ActionRow row : getActionRows()) {
			for (ActionComponent actionComponent : row.getActionComponents()) {
				final String id = actionComponent.getId();

				final InputData data = modalMaps.removeInput(id);
				if (data == null)
					throw new IllegalStateException("Modal component with id '%s' could not be found in the inputs created with the '%s' class".formatted(id, Modals.class.getSimpleName()));

				inputDataMap.put(id, data);
			}
		}

		modalMaps.insertModal(new ModalData(handlerName, userData, inputDataMap));

		return modal;
	}
}

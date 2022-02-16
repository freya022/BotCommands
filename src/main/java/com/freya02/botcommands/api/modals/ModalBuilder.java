package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.InputData;
import com.freya02.botcommands.internal.modals.ModalData;
import com.freya02.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import org.jetbrains.annotations.ApiStatus;
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

	/**
	 * {@inheritDoc}
	 *
	 * <p>You can still set a custom ID on this ModalBuilder, this is an <b>optional</b> step
	 *
	 * <br>This could be useful if this modal gets closed by the user by mistake, as Discord caches the inputs by its modal ID (and input IDs),
	 * keeping the same ID might help the user not having to type things again
	 *
	 * <p><b>Pay attention, if the ID is the same then it means that modals associated to that ID will be overwritten</b>,
	 * so you should do something like appending the interacting user's ID at the end of the modal ID
	 */
	@NotNull
	@Override
	public ModalBuilder setId(@NotNull String customId) {
		super.setId(customId);

		return this;
	}

	@NotNull
	@Override
	public Modal build() {
		final Modal modal = super.build();

		//Extract input data into this map
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

		modalMaps.insertModal(new ModalData(handlerName, userData, inputDataMap), getId());

		return modal;
	}
}

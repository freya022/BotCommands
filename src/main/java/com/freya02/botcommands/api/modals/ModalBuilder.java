package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.ModalData;
import com.freya02.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

		modalMaps.insertModal(new ModalData(handlerName, userData));

		return modal;
	}
}

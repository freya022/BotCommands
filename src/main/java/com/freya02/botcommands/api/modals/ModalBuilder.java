package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.ModalData;
import com.freya02.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ModalBuilder extends Modal.Builder {
	private final ModalMaps modalMaps;
	private final ModalData modalData = new ModalData();

	@ApiStatus.Internal
	public ModalBuilder(ModalMaps modalMaps, String title) {
		super("0");

		this.modalMaps = modalMaps;

		setTitle(title);
	}

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

		modalMaps.insertModal(modalData);

		return modal;
	}
}

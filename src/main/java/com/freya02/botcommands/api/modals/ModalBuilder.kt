package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.api.modals.annotations.ModalData;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.internal.modals.*;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal.Builder;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//TODO use kotlin lmao
public class ModalBuilder extends Builder {
	private final ModalMaps modalMaps;
	private IModalHandlerData handlerData;
	private ModalTimeoutInfo timeoutInfo;

	@ApiStatus.Internal
	public ModalBuilder(ModalMaps modalMaps, @NotNull String title) {
		super("0", title);

		this.modalMaps = modalMaps;
	}

	/**
	 * Binds the following handler (defined by {@link ModalHandler}) with its arguments
	 *
	 * <br>This step is optional if you do not wish to use methods for that
	 *
	 * @param handlerName The name of the modal handler, must be the same as your {@link ModalHandler}
	 * @param userData    The optional user data to be passed to the modal handler via {@link ModalData}
	 *
	 * @return This builder for chaining convenience
	 */
	public ModalBuilder bindTo(@NotNull String handlerName, Object... userData) {
		Checks.notNull(handlerName, "Modal handler name");
		Checks.notNull(userData, "Modal user data");

		this.handlerData = new PersistentModalHandlerData(handlerName, userData);

		return this;
	}

	/**
	 * Binds the following handler to this modal
	 *
	 * <br>This step is optional if you do not wish to use handlers for that
	 *
	 * @param handler The modal handler to run when the modal is used
	 *
	 * @return This builder for chaining convenience
	 */
	public ModalBuilder bindTo(@NotNull Function2<ModalInteractionEvent, Continuation<?>, Object> handler) {
		Checks.notNull(handler, "Modal handler");

		this.handlerData = new EphemeralModalHandlerData(handler);

		return this;
	}

	/**
	 * Sets the timeout for this modal, the modal will not be recognized after the timeout has passed
	 * <br>The timeout will start when the modal is built
	 *
	 * <p><b>It is extremely recommended to put a timeout on your modals</b>, if your user dismisses the modal, so, never uses it, the data of the modal could stay in your RAM indefinitely
	 *
	 * @param timeout   The amount of time in the supplied time unit before the modal is removed
	 * @param unit      The time unit of the timeout
	 * @param onTimeout The function to run when the timeout has been reached
	 *
	 * @return This builder for chaining convenience
	 */
	@NotNull
	public ModalBuilder setTimeout(long timeout, @NotNull TimeUnit unit, @NotNull Runnable onTimeout) {
		Checks.positive(timeout, "Timeout");
		Checks.notNull(unit, "Time unit");
		Checks.notNull(onTimeout, "On-timeout runnable");

		this.timeoutInfo = new ModalTimeoutInfo(timeout, unit, onTimeout);

		return this;
	}

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
		//Extract input data into this map
		final Map<String, InputData> inputDataMap = new HashMap<>();
		for (ActionRow row : getActionRows()) {
			for (ActionComponent actionComponent : row.getActionComponents()) {
				final String id = actionComponent.getId();

				final InputData data = modalMaps.consumeInput(id);
				if (data == null)
					throw new IllegalStateException("Modal component with id '%s' could not be found in the inputs created with the '%s' class".formatted(id, Modals.class.getSimpleName()));

				inputDataMap.put(id, data);
			}
		}

		final String actualId = modalMaps.insertModal(new PartialModalData(handlerData, inputDataMap, timeoutInfo), getId());

		setId(actualId);

		return new Modal(super.build(), modalMaps);
	}
}

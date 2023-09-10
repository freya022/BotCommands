package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.api.modals.annotations.ModalData;
import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class ModalBuilder extends net.dv8tion.jda.api.interactions.modals.Modal.Builder implements IModalBuilder {
	protected ModalBuilder(@NotNull String customId, @NotNull String title) {
		super(customId, title);
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
	@NotNull
	public abstract ModalBuilder bindTo(@NotNull String handlerName, @NotNull List<@Nullable Object> userData);

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
	@NotNull
	public final ModalBuilder bindTo(@NotNull String handlerName, @Nullable Object @NotNull ... userData) {
		return bindTo(handlerName, Arrays.asList(userData));
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
	@NotNull
	public abstract ModalBuilder bindTo(@NotNull Consumer<ModalInteractionEvent> handler);

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
	public abstract ModalBuilder setTimeout(long timeout, @NotNull TimeUnit unit, @NotNull Runnable onTimeout);

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
	public ModalBuilder setId(@NotNull String customId) {
		super.setId(customId);
		return this;
	}

	@NotNull
	protected final net.dv8tion.jda.api.interactions.modals.Modal jdaBuild() {
		return super.build();
	}

	@NotNull
	public abstract Modal build();
}

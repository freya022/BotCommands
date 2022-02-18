package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.InteractionConstraints;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Provides base for a pagination builder
 *
 * @param <T> Type of the implementor
 * @param <R> Type of the implementor {@link #build()} return type
 */
@SuppressWarnings("unchecked")
public abstract class BasicPaginationBuilder<T extends BasicPaginationBuilder<T, R>, R extends BasicPagination<R>> {
	protected InteractionConstraints constraints = new InteractionConstraints();
	protected TimeoutInfo<R> timeout;

	/**
	 * Sets the timeout for this pagination instance
	 * <br><b>On timeout, only the consumer is called, no message are deleted and it is up to you to clean up components with {@link BasicPagination#cleanup(BContext)}</b>
	 *
	 * <br><br>How to manipulate the message on timeout, for example you want to delete the message, or replace its content:
	 * <ul>
	 *     <li>For application commands: You can use the {@link IDeferrableCallback#getHook() Interaction hook} of application event</li>
	 *     <li>For text commands: You can use {@link BasicPagination#setMessage(Message)} when the message has been sent successfully, so in your queue success consumer,
	 *     you will then receive that same message in the {@link PaginationTimeoutConsumer} you have set</li>
	 * </ul>
	 *
	 * @param timeout     Amount of time before the timeout occurs
	 * @param timeoutUnit Unit of time for the supplied timeout
	 * @param onTimeout   The consumer fired on timeout, long operations should not run here
	 * @return This builder for chaining convenience
	 */
	public T setTimeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull PaginationTimeoutConsumer<R> onTimeout) {
		Checks.positive(timeout, "Timeout");
		Checks.notNull(onTimeout, "Timeout consumer");
		Checks.notNull(timeoutUnit, "Timeout TimeUnit");

		this.timeout = new TimeoutInfo<>(timeout, timeoutUnit, onTimeout);

		return (T) this;
	}

	/**
	 * Sets the interaction constraints for this pagination object
	 * <br>These constraints control who can use this pagination object
	 *
	 * @param constraints The {@link InteractionConstraints} for this pagination object
	 * @return This builder for chaining convenience
	 *
	 * @see InteractionConstraints Factory methods of InteractionConstraints
	 */
	public T setConstraints(@NotNull InteractionConstraints constraints) {
		this.constraints = constraints;

		return (T) this;
	}

	/**
	 * Builds this pagination instance
	 *
	 * @return The newly created pagination instance
	 */
	@NotNull
	public abstract R build();
}

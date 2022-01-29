package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @param <T> Type of the implementor
 */
public abstract class BasicPagination<T extends BasicPagination<T>> {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor();

	protected final InteractionConstraints constraints;
	@Nullable protected final TimeoutInfo<T> timeout;

	protected final MessageBuilder messageBuilder = new MessageBuilder();
	protected final PaginatorComponents components = new PaginatorComponents();

	private final Set<String> usedIds = new HashSet<>();

	@Nullable private ScheduledFuture<?> timeoutFuture;
	@Nullable private Message message;

	private boolean timeoutPassed = false;

	protected BasicPagination(@NotNull InteractionConstraints constraints, @Nullable TimeoutInfo<T> timeout) {
		this.constraints = constraints;
		this.timeout = timeout;
	}

	/**
	 * Returns the {@link Message} for this current page
	 *
	 * @return The {@link Message} for this current page
	 */
	public abstract Message get();

	/**
	 * Sets the {@link Message} associated to this paginator
	 * <br>This is an optional operation and will only provide the {@link Message} object through the {@link PaginationTimeoutConsumer} you have set in your paginator builder
	 * <br><b>This message instance is not updated, this should only help you get the message's ID and not what's inside it</b>
	 *
	 * @param message The {@link Message} object associated to this paginator
	 * @see BasicPaginationBuilder#setTimeout(long, TimeUnit, PaginationTimeoutConsumer)
	 */
	public void setMessage(@NotNull Message message) {
		this.message = message;
	}

	protected void onPreGet() {
		if (timeoutPassed && timeout != null) {
			LOGGER.warn("Timeout has already been cleaned up by pagination is still used ! Make sure you called BasicPagination#cleanup in the timeout consumer, timeout consumer at: {}", timeout.onTimeout().getClass().getNestHost());
		}

		messageBuilder.clear();
		components.clear();

		restartTimeout();
	}

	/**
	 * Restarts the timeout of this pagination instance
	 * <br>This means the timeout will be scheduled again, as if you called {@link #get()}, but without changing the actual content
	 */
	@SuppressWarnings("unchecked")
	public void restartTimeout() {
		if (timeout != null) {
			if (timeoutFuture != null) {
				timeoutFuture.cancel(false);
			}

			//Can't supply instance on by calling super constructor
			// Also don't want to do an abstract T getThis()
			timeoutFuture = TIMEOUT_SERVICE.schedule(() -> {
				timeoutPassed = true;

				timeout.onTimeout().accept((T) this, message);
			}, timeout.timeout(), timeout.unit());
		}
	}

	protected void onPostGet() {
		for (ActionRow row : components.getActionRows()) {
			for (ActionComponent component : row.getActionComponents()) {
				final String id = component.getId();

				if (id == null) continue;

				usedIds.add(id);
			}
		}
	}

	/**
	 * Cancels the timeout action for this pagination instance
	 * <br>The timeout will be enabled back if the page changes
	 */
	public void cancelTimeout() {
		if (timeoutFuture != null) {
			timeoutFuture.cancel(false);
		}
	}

	/**
	 * Cleans up the button IDs used in this paginator
	 * <br>This will remove every stored button IDs, even then buttons you included yourself
	 *
	 * @param context The {@link BContext} of this bot
	 */
	public void cleanup(BContext context) {
		final ComponentManager manager = Utils.getComponentManager(context);

		final int deletedIds = manager.deleteIds(usedIds);

		LOGGER.trace("Cleaned up {} component IDs out of {}", deletedIds, usedIds.size());

		usedIds.clear();
	}
}

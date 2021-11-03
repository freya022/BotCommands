package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.pagination.BasicPagination;
import com.freya02.botcommands.api.pagination.PaginationTimeoutConsumer;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InteractiveMenuBuilder {
	private final List<InteractiveMenuItem> items = new ArrayList<>();
	private long ownerId;
	private TimeoutInfo<InteractiveMenu> timeout;

	/**
	 * Adds a menu to this {@link InteractiveMenu}
	 * <br><b>Note: The first added menu will be the first selected one</b>
	 *
	 * @param content  The content of the {@link SelectOption} bound to this menu
	 * @param supplier The interactive menu supplier for this menu's page
	 * @return This builder for chaining convenience
	 * @see SelectContent#of(String, String, Emoji)
	 */
	public InteractiveMenuBuilder addMenu(@NotNull SelectContent content, @NotNull InteractiveMenuSupplier supplier) {
		items.add(new InteractiveMenuItem(content, supplier));

		return this;
	}

	/**
	 * Sets the timeout for this {@link InteractiveMenu}
	 * <br><b>On timeout, only the consumer is called, no message are deleted and it is up to you to clean up components with {@link BasicPagination#cleanup(BContext)}</b>
	 *
	 * <br><br>How to manipulate the message on timeout, for example you want to delete the message, or replace its content:
	 * <ul>
	 *     <li>For application commands: You can use the {@link Interaction#getHook() Interaction hook} of application event</li>
	 *     <li>For text commands: You can use {@link BasicPagination#setMessage(Message)} when the message has been sent successfully, so in your queue success consumer,
	 *     you will then receive that same message in the {@link PaginationTimeoutConsumer} you have set</li>
	 * </ul>
	 *
	 * @param timeout     Amount of time before the timeout occurs
	 * @param timeoutUnit Unit of time for the supplied timeout
	 * @param onTimeout   The consumer fired on timeout, long operations should not run here
	 * @return This builder for chaining convenience
	 */
	public InteractiveMenuBuilder setTimeout(long timeout, @NotNull TimeUnit timeoutUnit, @NotNull PaginationTimeoutConsumer<InteractiveMenu> onTimeout) {
		Checks.positive(timeout, "Timeout");
		Checks.notNull(onTimeout, "Timeout consumer");
		Checks.notNull(timeoutUnit, "Timeout TimeUnit");

		this.timeout = new TimeoutInfo<>(timeout, timeoutUnit, onTimeout);

		return this;
	}

	/**
	 * Set the owner ID for this {@link InteractiveMenu}
	 * <br>Only the user with this ID will be able to use this, if the value is not set, or 0, then everyone can use it
	 *
	 * @param ownerId The ID of the user which can use this
	 * @return This builder for chaining convenience
	 */
	public InteractiveMenuBuilder setOwnerId(long ownerId) {
		this.ownerId = ownerId;

		return this;
	}

	public InteractiveMenu build() {
		return new InteractiveMenu(items, ownerId, timeout);
	}
}

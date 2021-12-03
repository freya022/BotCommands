package com.freya02.botcommands.api.pagination.paginator;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.pagination.BasicPagination;
import com.freya02.botcommands.api.pagination.PaginatorSupplier;
import com.freya02.botcommands.api.pagination.TimeoutInfo;
import com.freya02.botcommands.api.utils.ButtonContent;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

/**
 * @param <T> Type of the implementor
 */
@SuppressWarnings("unchecked")
public abstract class BasicPaginator<T extends BasicPaginator<T>> extends BasicPagination<T> {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Message DELETED_MESSAGE = new MessageBuilder("[deleted]").build();
	protected final PaginatorSupplier supplier;
	private final int maxPages;
	private final Button deleteButton;
	protected int page = 0;
	private Button firstButton, previousButton, nextButton, lastButton;

	protected BasicPaginator(InteractionConstraints constraints, TimeoutInfo<T> timeout, int _maxPages, PaginatorSupplier supplier, boolean hasDeleteButton,
	                         ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent) {
		super(constraints, timeout);

		this.maxPages = _maxPages;
		this.supplier = supplier;

		firstButton = Components.primaryButton(e -> {
			page = 0;

			e.editMessage(get()).queue();
		}).setConstraints(constraints).build(firstContent);

		previousButton = Components.primaryButton(e -> {
			page = Math.max(0, page - 1);

			e.editMessage(get()).queue();
		}).setConstraints(constraints).build(previousContent);

		nextButton = Components.primaryButton(e -> {
			page = Math.min(maxPages - 1, page + 1);

			e.editMessage(get()).queue();
		}).setConstraints(constraints).build(nextContent);

		lastButton = Components.primaryButton(e -> {
			page = maxPages - 1;

			e.editMessage(get()).queue();
		}).setConstraints(constraints).build(lastContent);

		if (hasDeleteButton) {
			//Unique use in the case the message isn't ephemeral
			this.deleteButton = Components.dangerButton(this::onDeleteClicked).setConstraints(constraints).oneUse().build(deleteContent);
		} else {
			this.deleteButton = null;
		}
	}

	public int getPage() {
		return page;
	}

	/**
	 * Sets the page number, <b>this does not update the embed in any way</b>
	 *
	 * @param page Number of the page, from <code>0</code> to <code>maxPages - 1</code>
	 * @return This {@link Paginator} for chaining convenience
	 */
	public T setPage(int page) {
		Checks.check(page >= 0, "Page cannot be negative");
		Checks.check(page < maxPages, "Page cannot be higher than max page (%d)", maxPages);

		this.page = page;

		return (T) this;
	}

	private void onDeleteClicked(ButtonEvent e) {
		if (!e.getMessage().isEphemeral()) {
			e.deferEdit().queue();
			e.getMessage().delete().queue();
		} else {
			e.editMessage(DELETED_MESSAGE).queue();

			LOGGER.warn("Attempted to delete a ephemeral message using a Paginator delete button, consider disabling the delete button in the constructor or making your message not ephemeral, pagination supplier comes from {}", supplier.getClass().getName());
		}

		cleanup(e.getContext());
	}

	/**
	 * Returns the current page as a Message, you only need to use this once to send the initial embed. <br>
	 * The user can then use the navigation buttons to navigate.
	 *
	 * @return The {@link Message} for this Paginator
	 */
	@Override
	public Message get() {
		onPreGet();

		putComponents();

		final MessageEmbed embed = getEmbed();

		messageBuilder.setEmbeds(embed);

		final List<ActionRow> rows = components.getActionRows();
		messageBuilder.setActionRows(rows);

		onPostGet();

		return messageBuilder.build();
	}

	@NotNull
	protected MessageEmbed getEmbed() {
		return supplier.get(messageBuilder, components, page);
	}

	protected void putComponents() {
		if (page == 0) {
			previousButton = previousButton.asDisabled();
			firstButton = firstButton.asDisabled();
		} else {
			previousButton = previousButton.asEnabled();
			firstButton = firstButton.asEnabled();
		}

		if (page >= maxPages - 1) {
			nextButton = nextButton.asDisabled();
			lastButton = lastButton.asDisabled();
		} else {
			nextButton = nextButton.asEnabled();
			lastButton = lastButton.asEnabled();
		}

		if (deleteButton != null) {
			components.addComponents(0,
					firstButton,
					previousButton,
					nextButton,
					lastButton,
					deleteButton
			);
		} else {
			components.addComponents(0,
					firstButton,
					previousButton,
					nextButton,
					lastButton
			);
		}
	}
}

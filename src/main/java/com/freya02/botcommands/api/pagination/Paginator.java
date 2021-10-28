package com.freya02.botcommands.api.pagination;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.components.ComponentManager;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.pagination.menu.ButtonContent;
import com.freya02.botcommands.api.pagination.menu.ButtonContentSupplier;
import com.freya02.botcommands.api.pagination.menu.ChoiceMenuBuilder;
import com.freya02.botcommands.api.pagination.menu.Menu;
import com.freya02.botcommands.api.utils.EmojiUtils;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Provides a paginator - You provide the pages, it displays them one by one.<br>
 * Initial page is page 0, there is navigation buttons and an optional delete button<br>
 * <b>The delete button cannot be used if the message is ephemeral</b><br><br>
 *
 * <h2>The button IDs used by this paginator and those registered by the {@link PaginatorComponents} in the {@link PaginationSupplier} are cleaned up once the embed is removed with the button</h2>
 * <h3>Buttons that can delete this embed have to call {@link #cleanup(BContext)}</h3>
 *
 * @see Menu
 */
public class Paginator {
	private static final Logger LOGGER = Logging.getLogger();
	private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor();

	private static final Emoji FIRST_EMOJI = EmojiUtils.resolveJDAEmoji(":rewind:");
	private static final Emoji PREVIOUS_EMOJI = EmojiUtils.resolveJDAEmoji(":arrow_backward:");
	private static final Emoji NEXT_EMOJI = EmojiUtils.resolveJDAEmoji(":arrow_forward:");
	private static final Emoji LAST_EMOJI = EmojiUtils.resolveJDAEmoji(":fast_forward:");
	private static final Emoji DELETE_EMOJI = EmojiUtils.resolveJDAEmoji(":wastebasket:");
	private static final Message DELETED_MESSAGE = new MessageBuilder("[deleted]").build();

	private ButtonContent firstContent = ButtonContent.withEmoji(FIRST_EMOJI);
	private ButtonContent previousContent = ButtonContent.withEmoji(PREVIOUS_EMOJI);
	private ButtonContent nextContent = ButtonContent.withEmoji(NEXT_EMOJI);
	private ButtonContent lastContent = ButtonContent.withEmoji(LAST_EMOJI);
	private ButtonContent deleteContent = ButtonContent.withEmoji(DELETE_EMOJI);

	private final int maxPages;

	private final MessageBuilder messageBuilder = new MessageBuilder();
	private final Button deleteButton;
	private Button firstButton, previousButton, nextButton, lastButton;

	private final Set<String> usedIds = new HashSet<>();

	private long timeout;
	private TimeUnit timeoutUnit;
	private Future<?> timeoutFuture;
	private Consumer<Paginator> onTimeout;

	private String title, titleUrl;
	private int page = 0;
	private PaginationSupplier paginationSupplier = (builder, components, page1) -> {};

	/**
	 * Creates a new paginator
	 *
	 * @param userId             The ID of the only User who should be able to use this menu
	 * @param maxPages           Maximum amount of pages in this paginator
	 * @param deleteButton       Whether there should be a delete button on the {@link Paginator}
	 * @param paginationSupplier Supplies the pages for this paginator
	 */
	public Paginator(long userId, int maxPages, boolean deleteButton, PaginationSupplier paginationSupplier) {
		this(userId, maxPages, deleteButton);

		setPaginationSupplier(paginationSupplier);
	}

	/**
	 * Creates a new paginator
	 *
	 * @param userId       The ID of the only User who should be able to use this menu
	 *                     <br>An ID of 0 means this paginator will be usable by everyone
	 * @param maxPages     Maximum amount of pages in this paginator
	 * @param deleteButton Whether there should be a delete button on the {@link Paginator}
	 */
	public Paginator(long userId, int maxPages, boolean deleteButton) {
		this.maxPages = maxPages;

		firstButton = Components.primaryButton(e -> {
			page = 0;

			e.editMessage(get()).queue();
		}).ownerId(userId).build(firstContent);

		previousButton = Components.primaryButton(e -> {
			page = Math.max(0, page - 1);

			e.editMessage(get()).queue();
		}).ownerId(userId).build(previousContent);

		nextButton = Components.primaryButton(e -> {
			page = Math.min(maxPages - 1, page + 1);

			e.editMessage(get()).queue();
		}).ownerId(userId).build(nextContent);

		lastButton = Components.primaryButton(e -> {
			page = maxPages - 1;

			e.editMessage(get()).queue();
		}).ownerId(userId).build(lastContent);

		if (deleteButton) {
			//Unique use in the case the message isn't ephemeral
			this.deleteButton = Components.dangerButton(this::onDeleteClicked).ownerId(userId).oneUse().build(deleteContent);
		} else {
			this.deleteButton = null;
		}
	}

	/**
	 * Sets the content for the button which goes to the first page
	 *
	 * @param firstContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 * @see ChoiceMenuBuilder#setButtonContentSupplier(ButtonContentSupplier)
	 */
	public Paginator setFirstContent(ButtonContent firstContent) {
		this.firstContent = firstContent;

		return this;
	}

	/**
	 * Sets the content for the button which goes to the previous page
	 *
	 * @param previousContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 * @see ChoiceMenuBuilder#setButtonContentSupplier(ButtonContentSupplier)
	 */
	public Paginator setPreviousContent(ButtonContent previousContent) {
		this.previousContent = previousContent;

		return this;
	}

	/**
	 * Sets the content for the button which goes to the next page
	 *
	 * @param nextContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 * @see ChoiceMenuBuilder#setButtonContentSupplier(ButtonContentSupplier)
	 */
	public Paginator setNextContent(ButtonContent nextContent) {
		this.nextContent = nextContent;

		return this;
	}

	/**
	 * Sets the content for the button which goes to the last page
	 *
	 * @param lastContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 * @see ChoiceMenuBuilder#setButtonContentSupplier(ButtonContentSupplier)
	 */
	public Paginator setLastContent(ButtonContent lastContent) {
		this.lastContent = lastContent;

		return this;
	}

	/**
	 * Sets the content for the button which deletes this paginator
	 *
	 * @param deleteContent The {@link ButtonContent} for this button
	 * @return This {@link Paginator} for chaining convenience
	 * @see ChoiceMenuBuilder#setButtonContentSupplier(ButtonContentSupplier)
	 */
	public Paginator setDeleteContent(ButtonContent deleteContent) {
		this.deleteContent = deleteContent;

		return this;
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
	public Paginator setPage(int page) {
		Checks.check(page >= 0, "Page cannot be negative");
		Checks.check(page < maxPages, "Page cannot be higher than max page (%d)", maxPages);

		this.page = page;

		return this;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Sets the {@link EmbedBuilder#setTitle(String, String) title} used for the Paginator embed
	 *
	 * @param title The title of the Paginator embed
	 * @param url   The title's URL
	 * @return This {@link Paginator} for chaining convenience
	 * @see EmbedBuilder#setTitle(String, String)
	 */
	public Paginator setTitle(@Nullable String title, @Nullable String url) {
		this.title = title;
		this.titleUrl = url;

		return this;
	}

	/**
	 * Sets the {@link PaginationSupplier} - Gives the pages to this paginator
	 *
	 * @param paginationSupplier The {@link PaginationSupplier}
	 * @return This {@link Paginator} for chaining convenience
	 */
	public Paginator setPaginationSupplier(PaginationSupplier paginationSupplier) {
		Checks.notNull(paginationSupplier, "Pagination supplier");

		this.paginationSupplier = paginationSupplier;

		return this;
	}

	/**
	 * Sets the timeout for this {@link Paginator}
	 * <b>On timeout, only the consumer is called, no message are deleted and it is up to you to clean up buttons with {@link #cleanup(BContext)}</b>
	 *
	 * @param timeout     Amount of time before the timeout occurs
	 * @param timeoutUnit Unit of time for the supplied timeout
	 * @param onTimeout   The consumer fired on timeout
	 * @return This {@link Paginator} for chaining convenience
	 */
	public Paginator setTimeout(long timeout, TimeUnit timeoutUnit, Consumer<Paginator> onTimeout) {
		Checks.notNull(onTimeout, "Timeout consumer");
		Checks.notNull(timeoutUnit, "Timeout TimeUnit");

		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.onTimeout = onTimeout;

		return this;
	}

	private void onDeleteClicked(ButtonEvent e) {
		if (!e.getMessage().isEphemeral()) {
			e.deferEdit().queue();
			e.getMessage().delete().queue();
		} else {
			e.editMessage(DELETED_MESSAGE).queue();

			LOGGER.warn("Attempted to delete a ephemeral message using a Paginator delete button, consider disabling the delete button in the constructor or making your message not ephemeral, pagination supplier comes from {}", paginationSupplier.getClass().getName());
		}

		cleanup(e.getContext());
	}

	/**
	 * Cleans up the button IDs used in this paginator
	 *
	 * @param context The BContext of this bot
	 */
	public void cleanup(BContext context) {
		final ComponentManager manager = Utils.getComponentManager(context);

		final int deletedIds = manager.deleteIds(usedIds);

		LOGGER.trace("Cleaned up {} component IDs out of {}", deletedIds, usedIds.size());

		usedIds.clear();
	}

	/**
	 * Returns the current page as a Message, you only need to use this once to send the initial embed. <br>
	 * The user can then use the navigation buttons to navigate.
	 *
	 * @return The {@link Message} for this Paginator
	 */
	public Message get() {
		if (timeout > 0 && onTimeout != null) {
			if (timeoutFuture != null) {
				timeoutFuture.cancel(false);
			}

			timeoutFuture = TIMEOUT_SERVICE.schedule(() -> {
				if (onTimeout != null) {
					onTimeout.accept(this);
				}
			}, timeout, timeoutUnit);
		}

		messageBuilder.clear();

		final EmbedBuilder builder = new EmbedBuilder();
		final PaginatorComponents paginatorComponents = new PaginatorComponents();

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
			paginatorComponents.addComponents(0,
					firstButton,
					previousButton,
					nextButton,
					lastButton,
					deleteButton
			);
		} else {
			paginatorComponents.addComponents(0,
					firstButton,
					previousButton,
					nextButton,
					lastButton
			);
		}

		builder.setTitle(title, titleUrl);
		builder.setFooter("Page " + (page + 1) + "/" + maxPages);

		paginationSupplier.accept(builder, paginatorComponents, page);

		messageBuilder.setEmbeds(builder.build());

		final List<ActionRow> rows = paginatorComponents.getActionRows();
		messageBuilder.setActionRows(rows);

		for (ActionRow row : rows) {
			for (Component component : row.getComponents()) {
				if (component instanceof Button) {
					if (((Button) component).getStyle() != ButtonStyle.LINK) {
						final String id = component.getId();

						if (id == null) continue;

						usedIds.add(id);
					}
				}
			}
		}

		return messageBuilder.build();
	}
}

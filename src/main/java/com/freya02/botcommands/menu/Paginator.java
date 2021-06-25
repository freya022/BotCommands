package com.freya02.botcommands.menu;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.buttons.ButtonId;
import com.freya02.botcommands.buttons.IdManager;
import com.freya02.botcommands.utils.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
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

	private static final Emoji PREVIOUS_EMOJI = EmojiUtils.resolveJdaEmoji(":rewind:");
	private static final Emoji NEXT_EMOJI = EmojiUtils.resolveJdaEmoji(":fast_forward:");
	private static final Emoji DELETE_EMOJI = EmojiUtils.resolveJdaEmoji(":wastebasket:");
	private static final Message DELETED_MESSAGE = new MessageBuilder("[deleted]").build();

	private final int maxPages;

	private final MessageBuilder messageBuilder = new MessageBuilder();
	private final String nextButtonId, previousButtonId, deleteButtonId;

	private final Set<String> usedIds = new HashSet<>();

	private String title, titleUrl;
	private int page = 0;
	private PaginationSupplier paginationSupplier = (builder, components, page1) -> { };

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
	 * @param maxPages     Maximum amount of pages in this paginator
	 * @param deleteButton Whether there should be a delete button on the {@link Paginator}
	 */
	public Paginator(long userId, int maxPages, boolean deleteButton) {
		this.maxPages = maxPages;

		previousButtonId = ButtonId.ofUser(userId, (c, e) -> {
			page = Math.max(0, page - 1);

			e.editMessage(get()).queue();
		});

		nextButtonId = ButtonId.ofUser(userId, (c, e) -> {
			page = Math.min(maxPages - 1, page + 1);

			e.editMessage(get()).queue();
		});

		if (deleteButton) {
			//Unique use in the case the message isn't ephemeral
			// Do not use ButtonId#uniqueOfUser as button ids are deleted by Paginator#cleanup
			deleteButtonId = ButtonId.ofUser(userId, this::onDeleteClicked);
		} else {
			deleteButtonId = null;
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
	 * Sets the {@link EmbedBuilder#setTitle(String, String)} title} used for the Paginator embed
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

	private void onDeleteClicked(BContext context, ButtonClickEvent e) {
		if (e.getMessage() != null) {
			e.deferEdit().queue();
			e.getMessage().delete().queue();
		} else {
			e.editMessage(DELETED_MESSAGE).queue();

			LOGGER.warn("Attempted to delete a ephemeral message using a Paginator delete button, consider disabling the delete button in the constructor or making your message not ephemeral, pagination supplier comes from {}", paginationSupplier.getClass().getName());
		}

		cleanup(context);
	}

	public void cleanup(BContext context) {
		final IdManager manager = context.getIdManager();
		if (manager == null)
			throw new IllegalStateException("Cannot clean used button IDs as ID manager doesn't exist");

		manager.removeIds(usedIds);

		LOGGER.trace("Cleaned up {} button IDs", usedIds.size());

		usedIds.clear();
	}

	/**
	 * Returns the current page as a Message, you only need to use this once to send the initial embed. <br>
	 * The user can then use the navigation buttons to navigate.
	 *
	 * @return The {@link Message} for this Paginator
	 */
	public Message get() {
		messageBuilder.clear();

		final EmbedBuilder builder = new EmbedBuilder();
		final PaginatorComponents paginatorComponents = new PaginatorComponents();

		Button previousButton = Button.primary(previousButtonId, PREVIOUS_EMOJI);
		if (page == 0) previousButton = previousButton.asDisabled();

		Button nextButton = Button.primary(nextButtonId, NEXT_EMOJI);
		if (page >= maxPages - 1) nextButton = nextButton.asDisabled();

		if (deleteButtonId != null) {
			paginatorComponents.addComponents(0,
					previousButton,
					nextButton,
					Button.danger(deleteButtonId, DELETE_EMOJI)
			);
		} else {
			paginatorComponents.addComponents(0,
					previousButton,
					nextButton
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

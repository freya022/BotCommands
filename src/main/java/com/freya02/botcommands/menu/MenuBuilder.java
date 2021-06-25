package com.freya02.botcommands.menu;

import com.freya02.botcommands.buttons.ButtonId;
import com.freya02.botcommands.menu.transformer.EntryTransformer;
import com.freya02.botcommands.menu.transformer.StringTransformer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides a builder for {@link Menu}s
 *
 * @param <T> Type of the entries
 */
public class MenuBuilder<T> {
	private static final List<Emoji> emojis = IntStream.rangeClosed(1, 10)
			.mapToObj(i -> Emoji.fromUnicode(i + "\u20E3"))
			.collect(Collectors.toList());

	private final long userId;
	private final boolean deleteButton;

	private final List<T> entries;
	private final Map<Integer, MenuPage<T>> pages = new HashMap<>();
	private int maxEntriesPerPage = 5;
	private EntryTransformer<? super T> transformer = new StringTransformer();

	private PaginationSupplier paginationSupplier;
	private Consumer<T> callback;

	private Thread waitingThread;
	private T choice;

	/**
	 * Creates a new {@link Menu} builder
	 *
	 * @param userId       The ID of the only User who should be able to use this menu
	 * @param deleteButton Whether there should be a delete button on the {@link Paginator}
	 * @param entries      The entries which should be displayed to the user
	 */
	public MenuBuilder(long userId, boolean deleteButton, List<T> entries) {
		this.userId = userId;
		this.deleteButton = deleteButton;
		this.entries = entries;
	}

	/**
	 * Adds emojis to the emoji list<br>
	 * These emojis are used if there are more than 10 entries per page<br>
	 * So first emoji you'll insert is going to be entry #11, second is #12
	 *
	 * @param emojis The {@link Emoji} to insert
	 */
	public static void addIndexEmojis(Emoji... emojis) {
		Collections.addAll(MenuBuilder.emojis, emojis);
	}

	/**
	 * Sets the maximum number of entries per page<br>
	 * <b>This does not mean there will be X entries per page</b> but rather it will try to fit 5 entries maximum per page, if some text is too long it'll cut down the number of entries
	 *
	 * @param maxEntriesPerPage The maximum amount of entries per page
	 * @return This builder for chaining convenience
	 */
	public MenuBuilder<T> setMaxEntriesPerPage(int maxEntriesPerPage) {
		this.maxEntriesPerPage = maxEntriesPerPage;

		return this;
	}

	/**
	 * Here the pagination supplier is more about adding further more stuff in the embed, or more components<br>
	 * <b>The embed should be almost full so be aware that it might not fit into Discord limits</b>
	 *
	 * @param paginationSupplier The optional {@linkplain PaginationSupplier}
	 * @return This builder for chaining convenience
	 */
	public MenuBuilder<T> setPaginationSupplier(PaginationSupplier paginationSupplier) {
		Checks.notNull(paginationSupplier, "Pagination supplier");

		this.paginationSupplier = paginationSupplier;

		return this;
	}

	/**
	 * Sets the entry transformer for this menu
	 *
	 * @param transformer The {@link EntryTransformer} to use to stringify the entries
	 * @return This builder for chaining convenience
	 */
	public MenuBuilder<T> setTransformer(EntryTransformer<? super T> transformer) {
		Checks.notNull(transformer, "Entry transformer");

		this.transformer = transformer;

		return this;
	}

	/**
	 * Sets the callback for this menu
	 *
	 * @param callback The {@link Consumer} to call when the user makes their choice
	 * @return This builder for chaining convenience
	 */
	public MenuBuilder<T> setCallback(Consumer<T> callback) {
		Checks.notNull(callback, "Menu callback");

		this.callback = callback;

		return this;
	}

	/**
	 * Waits for the user to select something and return the chosen item
	 *
	 * @return The chosen item
	 */
	public synchronized T waitFor() {
		waitingThread = Thread.currentThread();

		try {
			wait();
		} catch (InterruptedException ignored) { }

		return choice;
	}

	private void makePages() {
		int page = 0;
		int oldEntry = 0;
		StringBuilder builder = new StringBuilder();

		for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
			T entry = entries.get(i);

			final String s = transformer.toString(entry);
			Checks.notLonger(s, MessageEmbed.TEXT_MAX_LENGTH - 8, "Entry #" + i + " string");

			if (i - oldEntry >= maxEntriesPerPage || builder.length() + s.length() > MessageEmbed.TEXT_MAX_LENGTH - 8) {
				pages.put(page, new MenuPage<>(builder.toString(), entries.subList(oldEntry, i)));

				page++;
				oldEntry = i;

				builder.setLength(0);
			}

			builder.append(emojis.get(i - oldEntry).getName()).append(" : ").append(s).append('\n');
		}

		pages.put(page, new MenuPage<>(builder.toString(), entries.subList(oldEntry, entries.size())));
	}

	public Menu build() {
		makePages();

		final Menu menu = new Menu(userId, pages.size(), deleteButton);

		menu.setMenuSupplier((builder, components, page) -> {
			final MenuPage<T> menuPage = pages.get(page);

			builder.setDescription(menuPage.getDescription());

			for (int i = 0; i < menuPage.getChoices().size(); i++) {
				final int choiceNumber = i;
				components.addComponents(1 + (i / 5), Button.primary(ButtonId.ofUser(userId, (context, e) -> {
					menu.cleanup(context);

					onChoiceClicked(menu, menuPage, choiceNumber, e);
				}), MenuBuilder.emojis.get(i)));
			}

			if (paginationSupplier != null) {
				paginationSupplier.accept(builder, components, page);
			}
		});

		return menu;
	}

	private void onChoiceClicked(Paginator menu, MenuPage<T> menuPage, int choiceNumber, ButtonClickEvent e) {
		this.choice = menuPage.getChoices().get(choiceNumber);

		if (e.getMessage() == null) { //Send a validation message for ephemeral replies
			final MessageBuilder messageBuilder = new MessageBuilder();
			final EmbedBuilder resultBuilder = new EmbedBuilder()
					.setTitle(menu.getTitle())
					.setDescription("You choosed: " + transformer.toString(choice));

			messageBuilder.setEmbeds(resultBuilder.build());

			e.editMessage(messageBuilder.build()).queue();
		} else { //If not ephemeral just delete and use callback
			e.getMessage().delete().queue();
		}

		if (callback != null) {
			callback.accept(choice);
		}

		if (waitingThread != null) {
			waitingThread.interrupt();
		}
	}
}
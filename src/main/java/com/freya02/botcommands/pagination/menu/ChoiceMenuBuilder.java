package com.freya02.botcommands.pagination.menu;

import com.freya02.botcommands.components.Components;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.pagination.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides a builder for {@link ChoiceMenu}s
 * <br>If a callback is <b>not</b> set, the menu will close itself after an item has been chosen, or get it's result displayed if the message is ephemeral
 * <br>You should use the callback to do something when a user chooses an entry. 
 *
 * @param <T> Type of the entries
 */
public class ChoiceMenuBuilder<T> extends BaseMenu<T, ChoiceMenuBuilder<T>> {
	private static final List<Emoji> emojis = IntStream.range(1, 10) //exclude 10 as its a special emoji
			.mapToObj(i -> Emoji.fromUnicode(i + "\u20E3"))
			.collect(Collectors.toCollection(() -> new ArrayList<>(10)));

	static {
		emojis.add(Emoji.fromUnicode("\uD83D\uDD1F"));
	}

	private BiConsumer<ButtonEvent, T> callback;

	private Thread waitingThread;
	private T choice;

	/**
	 * Creates a new {@link ChoiceMenu} builder
	 *
	 * @param userId       The ID of the only User who should be able to use this menu
	 *                     <br>An ID of 0 means this menu will be usable by everyone
	 * @param deleteButton Whether there should be a delete button on the {@link Paginator}
	 * @param entries      The entries which should be displayed to the user
	 */
	public ChoiceMenuBuilder(long userId, boolean deleteButton, List<T> entries) {
		super(userId, deleteButton, entries);
	}

	/**
	 * Adds emojis to the emoji list<br>
	 * These emojis are used if there are more than 10 entries per page<br>
	 * So first emoji you'll insert is going to be entry #11, second is #12
	 *
	 * @param emojis The {@link Emoji} to insert
	 */
	public static void addIndexEmojis(Emoji... emojis) {
		Collections.addAll(ChoiceMenuBuilder.emojis, emojis);
	}

	/**
	 * Sets the callback for this menu
	 *
	 * @param callback The {@link Consumer} to call when the user makes their choice
	 * @return This builder for chaining convenience
	 */
	public ChoiceMenuBuilder<T> setCallback(BiConsumer<ButtonEvent, T> callback) {
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

	public ChoiceMenu build() {
		makePages();

		final ChoiceMenu menu = new ChoiceMenu(userId, pages.size(), deleteButton);

		menu.setMenuSupplier((builder, components, page) -> {
			final MenuPage<T> menuPage = pages.get(page);

			builder.setDescription(menuPage.getDescription());

			for (int i = 0; i < menuPage.getChoices().size(); i++) {
				final int choiceNumber = i;

				final Button choiceButton = Components.primaryButton(event -> {
					menu.cleanup(event.getContext());

					onChoiceClicked(menu, menuPage, choiceNumber, event);
				}).ownerId(userId).build(ChoiceMenuBuilder.emojis.get(i));

				components.addComponents(1 + (i / 5), choiceButton);
			}

			if (paginationSupplier != null) {
				paginationSupplier.accept(builder, components, page);
			}
		});

		return menu;
	}

	private void onChoiceClicked(Paginator menu, MenuPage<T> menuPage, int choiceNumber, ButtonEvent e) {
		this.choice = menuPage.getChoices().get(choiceNumber);

		if (callback != null) {
			callback.accept(e, choice);
		} else {
			if (e.getMessage().isEphemeral()) { //Send a validation message for ephemeral replies
				final MessageBuilder messageBuilder = new MessageBuilder();
				final EmbedBuilder resultBuilder = new EmbedBuilder()
						.setTitle(menu.getTitle())
						.setDescription("You choosed: " + transformer.toString(choice));

				messageBuilder.setEmbeds(resultBuilder.build());

				e.editMessage(messageBuilder.build()).queue();
			} else { //If not ephemeral just delete and use callback
				e.getMessage().delete().queue();
			}
		}

		if (waitingThread != null) {
			waitingThread.interrupt();
		}
	}
}
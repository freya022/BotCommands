package io.github.freya022.botcommands.api.pagination.interactive;

import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.data.InteractionConstraints;
import io.github.freya022.botcommands.api.components.event.StringSelectEvent;
import io.github.freya022.botcommands.api.pagination.TimeoutInfo;
import io.github.freya022.botcommands.api.pagination.paginator.BasicPaginator;
import io.github.freya022.botcommands.api.utils.ButtonContent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @param <T> Type of the implementor
 */
@SuppressWarnings("unchecked")
public abstract class BasicInteractiveMenu<T extends BasicInteractiveMenu<T>> extends BasicPaginator<T> {
	protected final List<InteractiveMenuItem<T>> items;

	protected int selectedItem = 0;
	protected final boolean usePaginator;

	protected BasicInteractiveMenu(@NotNull Components componentsService,
								   InteractionConstraints constraints, TimeoutInfo<T> timeout, boolean hasDeleteButton,
								   ButtonContent firstContent, ButtonContent previousContent, ButtonContent nextContent, ButtonContent lastContent, ButtonContent deleteContent,
								   @NotNull List<InteractiveMenuItem<T>> items, boolean usePaginator) {
		super(componentsService, constraints, timeout, 0, (a, b, c, d) -> new EmbedBuilder().build(), hasDeleteButton, firstContent, previousContent, nextContent, lastContent, deleteContent);

		if (items.isEmpty()) throw new IllegalStateException("No interactive menu items has been added");

		this.usePaginator = usePaginator;
		this.items = items;
		setSelectedItem(0);
	}

	@NotNull
	protected StringSelectMenu createSelectMenu() {
		final SelectOption[] options = new SelectOption[items.size()];
		for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
			InteractiveMenuItem<T> item = items.get(i);

			SelectOption option = item.content().toSelectOption(String.valueOf(i));
			if (i == selectedItem) option = option.withDefault(true);

			options[i] = option;
		}

		return componentsService.ephemeralStringSelectMenu()
				.bindTo(this::onItemSelected)
				.oneUse(true)
				.constraints(constraints)
				.addOptions(options)
				.build();
	}

	private void onItemSelected(StringSelectEvent event) {
		selectedItem = Integer.parseInt(event.getValues().get(0));

		event.editMessage(get()).queue();
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public SelectContent getSelectedItemContent() {
		return items.get(selectedItem).content();
	}

	/**
	 * Sets the interactive menu item number, <b>this does not update the embed in any way</b>,
	 * you can use {@link #get()} with an {@code editOriginal} in order to update the embed on Discord
	 *
	 * @param itemIndex Index of the item, from {@code 0} to {@code [the number of menus] - 1}
	 *
	 * @return This instance for chaining convenience
	 */
	public T setSelectedItem(int itemIndex) {
		Checks.check(itemIndex >= 0, "Item index cannot be negative");
		Checks.check(itemIndex < items.size(), "Item index cannot be higher than max items count (%d)", items.size());

		this.selectedItem = itemIndex;
		setMaxPages(items.get(itemIndex).maxPages());
		setPage(0);

		return (T) this;
	}

	/**
	 * Sets the interactive menu item number, via it's label (O(n) search), <b>this does not update the embed in any way</b>,
	 * you can use {@link #get()} with an {@link InteractionHook#editOriginal(MessageEditData)} in order to update the embed on Discord
	 *
	 * @param itemLabel Label of the item, must be a valid label from any of the interactive menu items
	 *
	 * @return This instance for chaining convenience
	 */
	public T setSelectedItem(String itemLabel) {
		Checks.notEmpty(itemLabel, "Item name cannot be empty");

		for (int i = 0; i < items.size(); i++) {
			final String label = items.get(i).content().label();

			if (label.equals(itemLabel)) {
				return setSelectedItem(i);
			}
		}

		throw new IllegalArgumentException("Item name '" + itemLabel + "' cannot be found in this interactive menu");
	}

	@Override
	public MessageEditData get() {
		onPreGet();

		if (usePaginator) {
			putComponents();
		}

		components.addComponents(createSelectMenu());

		final MessageEmbed embed = items.get(selectedItem).supplier().get((T) this, getPage(), messageBuilder, components);
		messageBuilder.setEmbeds(embed);
		messageBuilder.setComponents(components.getActionRows());

		onPostGet();

		return messageBuilder.build();
	}
}

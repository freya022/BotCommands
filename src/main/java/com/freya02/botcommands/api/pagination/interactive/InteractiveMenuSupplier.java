package com.freya02.botcommands.api.pagination.interactive;

import com.freya02.botcommands.api.pagination.PaginatorComponents;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;

public interface InteractiveMenuSupplier {
	/**
	 * Returns the {@link MessageEmbed} for this interactive menu's page
	 *
	 * @param messageBuilder The {@link MessageBuilder} for this interactive menu, you can mostly ignore it but can use it to add attachments for examples, to use them in your embeds
	 * @param components The {@link PaginatorComponents} for this interactive menu's page, this allows you to add components on this page
	 *                   <br><b>Do not use {@link MessageBuilder#setActionRows(ActionRow...)} and such, these will be overridden by the menu</b>
	 * @return A {@link MessageEmbed} for this interactive menu's page
	 */
	@NotNull
	MessageEmbed get(@NotNull MessageBuilder messageBuilder, @NotNull PaginatorComponents components);
}

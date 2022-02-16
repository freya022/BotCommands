package com.freya02.botcommands.test.commands;

import com.freya02.botcommands.api.modals.annotations.ModalHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class ModalTest {
	//TODO tbh it's probably better if we wait until modals are fully implemented
	// Currently the modal component types are from Component.Type and ModalMapping only provides String(s)
	// Need to decide what will the method parameters will be, if we use parameter resolvers, etc...
	@ModalHandler(name = "modalHandler")
	public void onModal(@NotNull ModalInteractionEvent event) {
		event.reply("```java\n" + event
				.getValue("modal:formatting:code")
				.getAsString() + "```").queue();

		System.out.println();
	}
}

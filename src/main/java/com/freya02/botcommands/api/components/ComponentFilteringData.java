package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.BContext;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public record ComponentFilteringData(BContext context, GenericComponentInteractionCreateEvent event) {}

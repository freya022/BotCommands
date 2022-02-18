package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

public record ApplicationFilteringData(BContext context, GenericCommandInteractionEvent event, ApplicationCommandInfo commandInfo) {}

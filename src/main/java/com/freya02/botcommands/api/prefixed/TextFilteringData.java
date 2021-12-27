package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public record TextFilteringData(BContext context, MessageReceivedEvent event, TextCommandInfo commandInfo, String args) {}

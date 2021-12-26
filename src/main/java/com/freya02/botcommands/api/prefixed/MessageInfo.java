package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.prefixed.TextCommandInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageInfo {
	private final BContext context;
	private final MessageReceivedEvent event;
	private final TextCommandInfo commandInfo;
	private final String args;

	public MessageInfo(BContext context, MessageReceivedEvent event, TextCommandInfo commandInfo, String args) {
		this.context = context;
		this.event = event;
		this.commandInfo = commandInfo;
		this.args = args;
	}

	public MessageReceivedEvent getEvent() {
		return event;
	}

	public BContext getContext() {
		return context;
	}

	public TextCommandInfo getCommandInfo() {
		return commandInfo;
	}

	public String getArgs() {
		return args;
	}
}

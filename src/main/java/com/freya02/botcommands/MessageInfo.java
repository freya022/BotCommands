package com.freya02.botcommands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class MessageInfo {
	private final BContext context;
	private final GuildMessageReceivedEvent event;
	private final Command command;
	private final String args;

	public MessageInfo(BContext context, GuildMessageReceivedEvent event, Command command, String args) {
		this.context = context;
		this.event = event;
		this.command = command;
		this.args = args;
	}

	public GuildMessageReceivedEvent getEvent() {
		return event;
	}

	public BContext getContext() {
		return context;
	}

	public Command getCommand() {
		return command;
	}

	public String getArgs() {
		return args;
	}
}

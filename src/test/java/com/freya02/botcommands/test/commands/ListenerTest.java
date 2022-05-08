package com.freya02.botcommands.test.commands;

import com.freya02.botcommands.annotations.api.annotations.JDAEventListener;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

public class ListenerTest {
	//missing intent
//	@JDAEventListener
//	public void onJoin(GuildMemberJoinEvent event) {
//		throw new RuntimeException("Totally normal exception");
//	}

	//missing intent
//	@JDAEventListener
//	public void onJoin(GenericGuildMemberEvent event) {
//
//	}

	@JDAEventListener
	public void onJoin(GenericGuildEvent event) {}

	//missing intent
//	@JDAEventListener
//	public void onJoin(RawGatewayEvent event) {
//		System.out.println("ok " + event);
//	}
}

package io.github.freya022.botcommands.test.commands;

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

	public void onJoin(GenericGuildEvent event) {}

	//missing intent
//	@JDAEventListener
//	public void onJoin(RawGatewayEvent event) {
//		System.out.println("ok " + event);
//	}
}

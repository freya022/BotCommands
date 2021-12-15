package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;

import java.time.LocalDateTime;

public class CtorInjectionTest extends TextCommand {
	public CtorInjectionTest(BContext context, LocalDateTime start) {
		System.out.println("context = " + context);
		System.out.println("start = " + start);
	}

	@JDATextCommand(
			name = "ctorinjection"
	)
	public void run(CommandEvent event) {

	}
}
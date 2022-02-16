package com.freya02.botcommands.api.modals;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class TextInputBuilder extends TextInput.Builder {
	TextInputBuilder(String id, String label, TextInputStyle style) {
		super(id, label, style);
	}
}

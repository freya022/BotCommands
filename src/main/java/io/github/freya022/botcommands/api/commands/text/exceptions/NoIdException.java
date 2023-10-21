package io.github.freya022.botcommands.api.commands.text.exceptions;

public class NoIdException extends Exception {
	public NoIdException() {
		super("No supplied IMentionable", null, true, false);
	}
}
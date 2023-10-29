package io.github.freya022.botcommands.api.commands.text.exceptions;

public class BadIdException extends Exception {
	public BadIdException() {
		super("Not an IMentionable ID", null, true, false);
	}
}
package io.github.freya022.botcommands.api.commands.prefixed.exceptions;

public class NoIdException extends Exception {
	public NoIdException() {
		super("No supplied IMentionable", null, true, false);
	}
}
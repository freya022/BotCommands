package com.freya02.botcommands.api.commands.prefixed.exceptions;

public class BadIdException extends Exception {
	public BadIdException() {
		super("Not an IMentionable ID", null, true, false);
	}
}
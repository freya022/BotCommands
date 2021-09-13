package com.freya02.botcommands.api.prefixed.exceptions;

public class NoIdException extends Exception {
	public NoIdException() {
		super("No supplied IMentionable", null, true, false);
	}
}
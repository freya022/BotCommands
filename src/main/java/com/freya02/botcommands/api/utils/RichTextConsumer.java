package com.freya02.botcommands.api.utils;

@FunctionalInterface
public interface RichTextConsumer {
	void consume(String substring, RichTextType type);
}
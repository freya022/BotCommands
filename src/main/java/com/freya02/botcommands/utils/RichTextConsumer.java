package com.freya02.botcommands.utils;

@FunctionalInterface
public interface RichTextConsumer {
	void consume(String substring, RichTextType type);
}
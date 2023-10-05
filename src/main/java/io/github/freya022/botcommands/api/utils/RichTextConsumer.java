package io.github.freya022.botcommands.api.utils;

@FunctionalInterface
public interface RichTextConsumer {
	void consume(String substring, RichTextType type);
}
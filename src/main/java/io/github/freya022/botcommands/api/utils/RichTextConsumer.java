package io.github.freya022.botcommands.api.utils;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface RichTextConsumer {
    void consume(String substring, RichTextType type);
}
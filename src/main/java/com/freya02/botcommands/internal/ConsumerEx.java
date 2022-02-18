package com.freya02.botcommands.internal;

import java.util.function.Consumer;

/**
 * @see Consumer
 */
public interface ConsumerEx<T> {
	void accept(T t) throws Exception;
}

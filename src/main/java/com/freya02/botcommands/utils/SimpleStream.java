package com.freya02.botcommands.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * <p>Provides an {@linkplain InputStream} without exceptions</p>
 * <p>The stream can be closed without having to handle exceptions</p>
 * <p>If an exception occurs, then the <code>onException</code> consumer is used</p>
 * <p>This is particularly useful when closing streams in RestAction success / failure callbacks to reduce boilerplate code</p>
 */
public class SimpleStream extends InputStream {
	private final InputStream stream;
	private final Consumer<? super Throwable> onException;

	private SimpleStream(InputStream stream, Consumer<? super Throwable> onException) {
		this.stream = stream;
		this.onException = onException;
	}

	/** Constructs a new SimpleStream with the specified {@linkplain InputStream} and the exception consumer
	 * @param stream {@linkplain InputStream} to use when doing reading / closing operations
	 * @param onException {@linkplain Consumer} to use when an exception occurs when closing the stream
	 * @return A {@linkplain SimpleStream}
	 */
	public static SimpleStream of(@NotNull InputStream stream, @Nullable Consumer<? super Throwable> onException) {
		return new SimpleStream(stream, onException);
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public void close() {
		try {
			stream.close();
		} catch (Throwable e) {
			if (onException != null) {
				onException.accept(e);
			}
		}
	}
}

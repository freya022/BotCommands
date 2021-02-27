package com.freya02.botcommands.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <p>Provides an {@linkplain InputStream} without {@linkplain #close()} exceptions</p>
 * <p>The stream is closed automatically once unreachable and collected by the GC</p>
 * <p>If an exception occurs, then the <code>onException</code> consumer is used</p>
 * <p>This is particularly useful when closing streams after RestAction used them, to reduce boilerplate code</p>
 */
public class SimpleStream extends InputStream {
	private static final Cleaner CLEANER = Cleaner.create(r -> {
		final Thread thread = new Thread(r, "SimpleStream Cleaner Thread");
		thread.setDaemon(true);
		return thread;
	});

	private final InputStream stream;
	private final Consumer<? super Throwable> onException;

	private SimpleStream(InputStream stream, @Nullable Consumer<? super Throwable> onException) {
		this.stream = Objects.requireNonNull(stream, "Input stream is null");
		this.onException = onException;

		CLEANER.register(this, () -> {
			try {
				stream.close();
			} catch (IOException e) {
				if (onException != null) {
					onException.accept(e);
				}
			}
		});
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

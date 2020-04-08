package com.freya02.botcommands.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class SimpleStream extends InputStream {
	private final InputStream stream;
	private final Consumer<? super Throwable> onException;

	private SimpleStream(InputStream stream, Consumer<? super Throwable> onException) {
		this.stream = stream;
		this.onException = onException;
	}

	public static SimpleStream of(InputStream stream, Consumer<? super Throwable> onException) {
		return new SimpleStream(stream, onException);
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}

	public void close() {
		try {
			stream.close();
		} catch (Throwable e) {
			onException.accept(e);
		}
	}
}

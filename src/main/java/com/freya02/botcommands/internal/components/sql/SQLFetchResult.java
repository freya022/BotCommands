package com.freya02.botcommands.internal.components.sql;

import com.freya02.botcommands.api.components.FetchResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;

public class SQLFetchResult extends FetchResult {
	private final Connection connection;
	private boolean closed;

	public SQLFetchResult(@Nullable SQLFetchedComponent fetchedComponent, @NotNull Connection connection) {
		super(fetchedComponent);

		this.connection = connection;
	}

	public Connection getConnection() {
		if (closed) throw new IllegalStateException("Cannot get Connection from SQLFetchedComponent as it has been closed");

		return connection;
	}

	@Override
	@Nullable
	public SQLFetchedComponent getFetchedComponent() {
		return (SQLFetchedComponent) super.getFetchedComponent();
	}

	@Override
	public void close() throws Exception {
		closed = true;

		connection.close();
	}
}

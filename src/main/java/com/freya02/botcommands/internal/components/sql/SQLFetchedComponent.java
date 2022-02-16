package com.freya02.botcommands.internal.components.sql;

import com.freya02.botcommands.api.components.ComponentType;
import com.freya02.botcommands.api.components.FetchedComponent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLFetchedComponent implements FetchedComponent {
	private final ComponentType type;
	private final ResultSet resultSet;

	public SQLFetchedComponent(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;

		final int typeRaw = resultSet.getInt("type");
		this.type = ComponentType.fromKey(typeRaw);

		if (this.type == null) throw new IllegalArgumentException("Couldn't get type for " + typeRaw);
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	@Override
	@NotNull
	public ComponentType getType() {
		return type;
	}
}

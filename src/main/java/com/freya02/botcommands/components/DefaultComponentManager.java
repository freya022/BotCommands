package com.freya02.botcommands.components;

import com.freya02.botcommands.Logging;
import com.freya02.botcommands.components.builder.*;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.components.event.SelectionEvent;
import com.freya02.botcommands.components.internal.HandleComponentResult;
import com.freya02.botcommands.components.internal.data.LambdaButtonData;
import com.freya02.botcommands.components.internal.data.LambdaSelectionMenuData;
import com.freya02.botcommands.components.internal.data.PersistentButtonData;
import com.freya02.botcommands.components.internal.data.PersistentSelectionMenuData;
import com.freya02.botcommands.components.internal.sql.SqlComponentData;
import com.freya02.botcommands.components.internal.sql.SqlLambdaComponentData;
import com.freya02.botcommands.components.internal.sql.SqlLambdaCreateResult;
import com.freya02.botcommands.components.internal.sql.SqlPersistentComponentData;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultComponentManager implements ComponentManager {
	private static final Logger LOGGER = Logging.getLogger();

	private final ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();

	private final Supplier<Connection> connectionSupplier;

	private final Map<Long, Consumer<ButtonEvent>> buttonLambdaMap = new HashMap<>();
	private final Map<Long, Consumer<SelectionEvent>> selectionMenuLambdaMap = new HashMap<>();

	public DefaultComponentManager(@NotNull Supplier<@NotNull Connection> connectionSupplier) {
		this.connectionSupplier = connectionSupplier;

		try {
			setupTables();

			deleteTemporaryEntities();
		} catch (SQLException e) {
			LOGGER.error("Unable to create DefaultComponentManager", e);

			throw new RuntimeException("Unable to create DefaultComponentManager", e);
		}
	}

	@Override
	@Nullable
	public ComponentType getIdType(String id) {
		try (Connection connection = getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(
				     "select type from componentdata where componentid = ? limit 1;"
		     )) {
			preparedStatement.setString(1, id);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return ComponentType.fromKey(resultSet.getInt("type"));
				} else {
					return null;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Unable to get the ID type of '{}'", id);

			return null;
		}
	}

	@Override
	public void handleLambdaButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer) {
		handleLambdaComponent(event,
				onError,
				dataConsumer,
				buttonLambdaMap,
				LambdaButtonData::new);
	}

	@Override
	public void handleLambdaSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer) {
		handleLambdaComponent(event,
				onError,
				dataConsumer,
				selectionMenuLambdaMap,
				LambdaSelectionMenuData::new);
	}

	@SuppressWarnings("DuplicatedCode")
	private <EVENT extends GenericComponentInteractionCreateEvent, DATA> void handleLambdaComponent(GenericComponentInteractionCreateEvent event,
	                                                                                                Consumer<ComponentErrorReason> onError,
	                                                                                                Consumer<DATA> dataConsumer,
	                                                                                                Map<Long, Consumer<EVENT>> map,
	                                                                                                Function<Consumer<EVENT>, DATA> eventFunc) {
		try (Connection connection = getConnection()) {
			final SqlLambdaComponentData data = SqlLambdaComponentData.read(connection, event.getComponentId());

			if (data == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final HandleComponentResult result = handleComponentData(event, data);

			if (result.getErrorReason() != null) {
				onError.accept(result.getErrorReason());

				return;
			}

			final long handlerId = data.getHandlerId();

			final Consumer<EVENT> consumer;
			if (result.shouldDelete()) {
				data.delete(connection);

				consumer = map.remove(handlerId);
			} else {
				consumer = map.get(handlerId);
			}

			if (consumer == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				LOGGER.warn("Could not find a consumer for handler id {} on component {}", handlerId, event.getComponentId());

				return;
			}

			dataConsumer.accept(eventFunc.apply(consumer));
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a lambda component", e);

			throw new RuntimeException("An exception occurred while handling a lambda component", e);
		}
	}

	@Override
	public void handlePersistentButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer) {
		handlePersistentComponent(event,
				onError,
				dataConsumer,
				PersistentButtonData::new);
	}

	@Override
	public void handlePersistentSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer) {
		handlePersistentComponent(event,
				onError,
				dataConsumer,
				PersistentSelectionMenuData::new);
	}

	@SuppressWarnings("DuplicatedCode")
	private <DATA> void handlePersistentComponent(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<DATA> dataConsumer, BiFunction<String, String[], DATA> dataFunction) {
		try (Connection connection = getConnection()) {
			final SqlPersistentComponentData data = SqlPersistentComponentData.read(connection, event.getComponentId());

			if (data == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final HandleComponentResult result = handleComponentData(event, data);

			if (result.getErrorReason() != null) {
				onError.accept(result.getErrorReason());

				return;
			}

			final String handlerName = data.getHandlerName();
			final String[] args = data.getArgs();

			if (result.shouldDelete()) {
				data.delete(connection);
			}

			dataConsumer.accept(dataFunction.apply(handlerName, args));
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a persistent component", e);

			throw new RuntimeException("An exception occurred while handling a persistent component", e);
		}
	}

	private void scheduleLambdaTimeout(long timeout, long handlerId, String componentId) {
		timeoutService.schedule(() -> {
			try (Connection connection = getConnection()) {
				final SqlLambdaComponentData data = SqlLambdaComponentData.read(connection, componentId);
				if (data != null) {
					buttonLambdaMap.remove(handlerId);

					data.delete(connection);
				}
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("An error occurred while deleting a lambda component after a timeout", e);
				} else {
					e.printStackTrace();
				}
			}
		}, timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	@NotNull
	public String putLambdaButton(LambdaButtonBuilder builder) {
		try (Connection connection = getConnection()) {
			final SqlLambdaCreateResult result = SqlLambdaComponentData.create(connection,
					ComponentType.LAMBDA_BUTTON,
					builder.isOneUse(),
					builder.getOwnerId(),
					builder.getTimeout());

			buttonLambdaMap.put(result.getHandlerId(), builder.getConsumer());

			if (builder.getTimeout() > 0) {
				scheduleLambdaTimeout(builder.getTimeout(), result.getHandlerId(), result.getComponentId());
			}

			return result.getComponentId();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a lambda component", e);

			throw new RuntimeException("An exception occurred while registering a lambda component", e);
		}
	}

	@Override
	@NotNull
	public String putLambdaSelectionMenu(LambdaSelectionMenuBuilder builder) {
		try (Connection connection = getConnection()) {
			final SqlLambdaCreateResult result = SqlLambdaComponentData.create(connection,
					ComponentType.LAMBDA_SELECTION_MENU,
					builder.isOneUse(),
					builder.getOwnerId(),
					builder.getTimeout());

			selectionMenuLambdaMap.put(result.getHandlerId(), builder.getConsumer());

			if (builder.getTimeout() > 0) {
				scheduleLambdaTimeout(builder.getTimeout(), result.getHandlerId(), result.getComponentId());
			}

			return result.getComponentId();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a lambda component", e);

			throw new RuntimeException("An exception occurred while registering a lambda component", e);
		}
	}

	private void schedulePersistentTimeout(long timeout, String componentId) {
		timeoutService.schedule(() -> {
			try (Connection connection = getConnection()) {
				final SqlPersistentComponentData data = SqlPersistentComponentData.read(connection, componentId);
				if (data != null) {
					data.delete(connection);
				}
			} catch (SQLException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("An error occurred while deleting a persistent component after a timeout", e);
				} else {
					e.printStackTrace();
				}
			}
		}, timeout, TimeUnit.MILLISECONDS);
	}

	private <T extends ComponentBuilder<T> & PersistentComponentBuilder> String putPersistentComponent(T builder, ComponentType type) {
		try (Connection connection = getConnection()) {
			final String componentId = SqlPersistentComponentData.create(connection,
					type,
					builder.isOneUse(),
					builder.getOwnerId(),
					builder.getTimeout(),
					builder.getHandlerName(),
					builder.getArgs());

			schedulePersistentTimeout(builder.getTimeout(), componentId);

			return componentId;
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a persistent component", e);

			throw new RuntimeException("An exception occurred while registering a persistent component", e);
		}
	}

	@Override
	@NotNull
	public String putPersistentButton(PersistentButtonBuilder builder) {
		return putPersistentComponent(builder, ComponentType.PERSISTENT_BUTTON);
	}

	@Override
	@NotNull
	public String putPersistentSelectionMenu(PersistentSelectionMenuBuilder builder) {
		return putPersistentComponent(builder, ComponentType.PERSISTENT_SELECTION_MENU);
	}

	@Override
	public void registerGroup(Collection<String> ids) {
		try (Connection connection = getConnection();
		     PreparedStatement updateGroupsStatement = connection.prepareStatement(
				     "select nextval('group_seq');\n" +
						     "update componentdata set groupid = currval('group_seq') where componentid = any(?);"
		     )) {
			updateGroupsStatement.setArray(1, connection.createArrayOf("text", ids.toArray()));

			updateGroupsStatement.execute();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a lambda component", e);

			throw new RuntimeException("An exception occurred while handling a lambda component", e);
		}
	}

	@Override
	public int deleteIds(Collection<String> ids) {
		if (ids.isEmpty()) return 0;

		try (Connection connection = getConnection()) {
			final Object[] idArray = ids.toArray();

			try (PreparedStatement preparedStatement = connection.prepareStatement(
					"delete from lambdacomponentdata where componentid = any(?) returning handlerid;"
			)) {
				preparedStatement.setArray(1, connection.createArrayOf("text", idArray));

				final ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					final long handlerId = resultSet.getLong("handlerId");

					//handler id is actually a shared sequence so there can't be duplicates even if we merged both maps
					if (buttonLambdaMap.remove(handlerId) == null) {
						selectionMenuLambdaMap.remove(handlerId);
					}
				}
			}

			try (PreparedStatement preparedStatement = connection.prepareStatement(
					//should be a cascade delete, see table declaration
					"delete from componentdata where componentid = any(?);"
			)) {
				preparedStatement.setArray(1, connection.createArrayOf("text", idArray));

				return preparedStatement.executeUpdate();
			}
		} catch (Exception e) {
			LOGGER.error("An exception occurred while deleting components", e);

			throw new RuntimeException("An exception occurred while deleting components", e);
		}
	}

	@NotNull
	private Connection getConnection() {
		return connectionSupplier.get();
	}

	private void deleteTemporaryEntities() throws SQLException {
		try (Connection connection = getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(
				     "delete from componentdata where type = ? or type = ?"
		     )) {
			preparedStatement.setInt(1, ComponentType.LAMBDA_BUTTON.getKey());
			preparedStatement.setInt(2, ComponentType.LAMBDA_SELECTION_MENU.getKey());

			preparedStatement.execute();
		}
	}

	private void setupTables() throws SQLException {
		try (Connection connection = getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(
				     "drop table if exists LambdaComponentData;\n" +
						     "\n" +
						     "create sequence if not exists group_seq as bigint;\n" +
						     "\n" +
						     "create table if not exists ComponentData\n" +
						     "(\n" +
						     "    componentId         text not null primary key,\n" +
						     "    type                int  not null,\n" +
						     "    groupId             bigint,\n" +
						     "    oneUse              bool not null,\n" +
						     "    ownerId             bigint,\n" +
						     "    expirationTimestamp bigint\n" +
						     ");\n" +
						     "\n" +
						     "create table LambdaComponentData\n" +
						     "(\n" +
						     "    componentId text   not null references ComponentData on delete cascade,\n" +
						     "    handlerId   serial8 not null\n" +
						     ");\n" +
						     "\n" +
						     "create table if not exists PersistentComponentData\n" +
						     "(\n" +
						     "    componentId text not null references ComponentData on delete cascade,\n" +
						     "    handlerName text not null,\n" +
						     "    args        text not null\n" +
						     ");"
		     )) {
			preparedStatement.execute();
		}
	}

	@NotNull
	private HandleComponentResult handleComponentData(GenericComponentInteractionCreateEvent event, SqlComponentData data) {
		final boolean oneUse = data.isOneUse() || data.getGroupId() > 0;
		final long ownerId = data.getOwnerId();
		final long expirationTimestamp = data.getExpirationTimestamp();

		if (expirationTimestamp > 0 && System.currentTimeMillis() > expirationTimestamp) {
			return new HandleComponentResult(ComponentErrorReason.EXPIRED, true);
		}

		if (ownerId > 0 && event.getUser().getIdLong() != ownerId) {
			return new HandleComponentResult(ComponentErrorReason.NOT_OWNER, false);
		}

		return new HandleComponentResult(null, oneUse);
	}
}
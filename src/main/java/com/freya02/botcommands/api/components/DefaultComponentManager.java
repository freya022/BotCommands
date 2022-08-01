package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.builder.*;
import com.freya02.botcommands.api.components.data.LambdaButtonData;
import com.freya02.botcommands.api.components.data.LambdaSelectionMenuData;
import com.freya02.botcommands.api.components.data.PersistentButtonData;
import com.freya02.botcommands.api.components.data.PersistentSelectionMenuData;
import com.freya02.botcommands.core.internal.db.Database;
import com.freya02.botcommands.internal.components.HandleComponentResult;
import com.freya02.botcommands.internal.components.sql.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Default implementation using a Postgresql database
 * <br><b>The database needs to be setup before usage, and upgraded if necessary</b>, the framework should exit and indicate instructions if the version isn't correct.
 */
public class DefaultComponentManager implements ComponentManager {
	private static final Logger LOGGER = Logging.getLogger();

	private final ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();

	private final Database database;

	private final Map<Long, ButtonConsumer> buttonLambdaMap = new HashMap<>();
	private final Map<Long, SelectionConsumer> selectionMenuLambdaMap = new HashMap<>();

	public DefaultComponentManager(@NotNull Database database) throws SQLException {
		this.database = database;

		try (Connection connection = getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(
					"delete from bc_component_data using bc_lambda_component_data where type in (1, 3);"
			)) {
				statement.executeUpdate();
			}
		}
	}

	@Override
	@NotNull
	public SQLFetchResult fetchComponent(String id) {
		final Connection connection = getConnection();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"select * " +
							"from bc_component_data " +
							"left join bc_lambda_component_data using (component_id) " +
							"left join bc_persistent_component_data using (component_id)" +
							"where component_id = ? " +
							"limit 1;"
			);
			preparedStatement.setString(1, id);

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return new SQLFetchResult(new SQLFetchedComponent(resultSet), connection);
			} else {
				return new SQLFetchResult(null, connection);
			}
		} catch (SQLException e) {
			LOGGER.error("Unable to get the ID type of '{}'", id);

			return new SQLFetchResult(null, connection);
		}
	}

	@Override
	public void handleLambdaButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer) {
		handleLambdaComponent(event,
				(SQLFetchResult) fetchResult,
				onError,
				dataConsumer,
				buttonLambdaMap,
				LambdaButtonData::new);
	}

	@Override
	public void handleLambdaSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer) {
		handleLambdaComponent(event,
				(SQLFetchResult) fetchResult,
				onError,
				dataConsumer,
				selectionMenuLambdaMap,
				LambdaSelectionMenuData::new);
	}

	private <CONSUMER extends ComponentConsumer<EVENT>,EVENT extends GenericComponentInteractionCreateEvent, DATA> void handleLambdaComponent(GenericComponentInteractionCreateEvent event,
	                                                                                                                                          SQLFetchResult fetchResult,
	                                                                                                                                          Consumer<ComponentErrorReason> onError,
	                                                                                                                                          Consumer<DATA> dataConsumer,
	                                                                                                                                          Map<Long, CONSUMER> map, Function<CONSUMER, DATA> eventFunc) {

		try {
			final SQLFetchedComponent fetchedComponent = fetchResult.getFetchedComponent();
			if (fetchedComponent == null)
				throw new IllegalArgumentException("A null fetched component cannot be handled");

			final SQLLambdaComponentData data = SQLLambdaComponentData.fromFetchedComponent(fetchedComponent);

			final HandleComponentResult result = handleComponentData(event, data);

			if (result.getErrorReason() != null) {
				onError.accept(result.getErrorReason());

				return;
			}

			final long handlerId = data.getHandlerId();

			final CONSUMER consumer;
			if (result.shouldDelete()) {
				try (Connection connection = getConnection()) {
					data.delete(connection);
				}

				consumer = map.remove(handlerId);
			} else {
				consumer = map.get(handlerId);
			}

			if (consumer == null) {
				throw new IllegalArgumentException("Could not find a consumer for handler id %s on component %s".formatted(handlerId, event.getComponentId()));
			}

			dataConsumer.accept(eventFunc.apply(consumer));
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a lambda component", e);

			throw new RuntimeException("An exception occurred while handling a lambda component", e);
		}
	}

	@Override
	public void handlePersistentButton(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer) {
		handlePersistentComponent(event,
				(SQLFetchResult) fetchResult,
				onError,
				dataConsumer,
				PersistentButtonData::new);
	}

	@Override
	public void handlePersistentSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer) {
		handlePersistentComponent(event,
				(SQLFetchResult) fetchResult,
				onError,
				dataConsumer,
				PersistentSelectionMenuData::new);
	}

	private <DATA> void handlePersistentComponent(GenericComponentInteractionCreateEvent event, SQLFetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<DATA> dataConsumer, BiFunction<String, String[], DATA> dataFunction) {
		try {
			final SQLFetchedComponent fetchedComponent = fetchResult.getFetchedComponent();
			if (fetchedComponent == null)
				throw new IllegalArgumentException("A null fetched component cannot be handled");

			final SQLPersistentComponentData data = SQLPersistentComponentData.fromFetchedComponent(fetchedComponent);

			final HandleComponentResult result = handleComponentData(event, data);

			if (result.getErrorReason() != null) {
				onError.accept(result.getErrorReason());

				return;
			}

			final String handlerName = data.getHandlerName();
			final String[] args = data.getArgs();

			if (result.shouldDelete()) {
				data.delete(fetchResult.getConnection());
			}

			dataConsumer.accept(dataFunction.apply(handlerName, args));
		} catch (Exception e) {
			LOGGER.error("An exception occurred while handling a persistent component", e);

			throw new RuntimeException("An exception occurred while handling a persistent component", e);
		}
	}

	private void scheduleLambdaTimeout(final Map<Long, ?> map, LambdaComponentTimeoutInfo timeout, long handlerId, String componentId) {
		if (timeout.timeout() > 0) {
			timeoutService.schedule(() -> {
				try (Connection connection = getConnection()) {
					final SQLLambdaComponentData data = SQLLambdaComponentData.read(connection, componentId);

					if (data != null) {
						map.remove(handlerId);

						data.delete(connection);

						timeout.timeoutCallback().run();
					}
				} catch (SQLException e) {
					LOGGER.error("An error occurred while deleting a lambda component after a timeout", e);
				}
			}, timeout.timeout(), timeout.timeoutUnit());
		}
	}

	@Override
	@NotNull
	public String putLambdaButton(LambdaButtonBuilder builder) {
		try (Connection connection = getConnection()) {
			final SQLLambdaCreateResult result = SQLLambdaComponentData.create(connection,
					ComponentType.LAMBDA_BUTTON,
					builder.isOneUse(),
					builder.getInteractionConstraints(),
					builder.getTimeout());

			buttonLambdaMap.put(result.handlerId(), builder.getConsumer());

			if (builder.getTimeout().timeout() > 0) {
				scheduleLambdaTimeout(buttonLambdaMap, builder.getTimeout(), result.handlerId(), result.componentId());
			}

			return result.componentId();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a lambda component", e);

			throw new RuntimeException("An exception occurred while registering a lambda component", e);
		}
	}

	@Override
	@NotNull
	public String putLambdaSelectMenu(LambdaSelectionMenuBuilder builder) {
		try (Connection connection = getConnection()) {
			final LambdaComponentTimeoutInfo timeout = builder.getTimeout();
			final SQLLambdaCreateResult result = SQLLambdaComponentData.create(connection,
					ComponentType.LAMBDA_SELECTION_MENU,
					builder.isOneUse(),
					builder.getInteractionConstraints(),
					timeout);

			selectionMenuLambdaMap.put(result.handlerId(), builder.getConsumer());

			if (timeout.timeout() > 0) {
				scheduleLambdaTimeout(selectionMenuLambdaMap, timeout, result.handlerId(), result.componentId());
			}

			return result.componentId();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a lambda component", e);

			throw new RuntimeException("An exception occurred while registering a lambda component", e);
		}
	}

	private void schedulePersistentTimeout(PersistentComponentTimeoutInfo timeout, String componentId) {
		if (timeout.timeout() > 0) {
			timeoutService.schedule(() -> {
				try (Connection connection = getConnection()) {
					final SQLPersistentComponentData data = SQLPersistentComponentData.read(connection, componentId);

					if (data != null) {
						data.delete(connection);
					}
				} catch (SQLException e) {
					LOGGER.error("An error occurred while deleting a persistent component after a timeout", e);
				}
			}, timeout.timeout(), timeout.timeoutUnit());
		}
	}

	private <T extends ComponentBuilder<T> & PersistentComponentBuilder<T>> String putPersistentComponent(T builder, ComponentType type) {
		try (Connection connection = getConnection()) {
			final String componentId = SQLPersistentComponentData.create(connection,
					type,
					builder.isOneUse(),
					builder.getInteractionConstraints(),
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
	public String putPersistentSelectMenu(PersistentSelectionMenuBuilder builder) {
		return putPersistentComponent(builder, ComponentType.PERSISTENT_SELECTION_MENU);
	}

	@Override
	public void registerGroup(Collection<String> ids) {
		try (Connection connection = getConnection();
		     PreparedStatement updateGroupsStatement = connection.prepareStatement(
				     "select nextval('bc_component_group_seq');\n" +
						     "update bc_component_data set group_id = currval('bc_component_group_seq') where component_id = any(?);"
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
					"delete from bc_lambda_component_data where component_id = any(?) returning handler_id;"
			)) {
				preparedStatement.setArray(1, connection.createArrayOf("text", idArray));

				final ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					final long handlerId = resultSet.getLong("handler_id");

					//handler id is actually a shared sequence so there can't be duplicates even if we merged both maps
					if (buttonLambdaMap.remove(handlerId) == null) {
						selectionMenuLambdaMap.remove(handlerId);
					}
				}
			}

			try (PreparedStatement preparedStatement = connection.prepareStatement(
					//should be a cascade delete, see table declaration
					"delete from bc_component_data where component_id = any(?);"
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
		return database.fetchConnection();
	}

	@NotNull
	private HandleComponentResult handleComponentData(GenericComponentInteractionCreateEvent event, SQLComponentData data) {
		final boolean oneUse = data.isOneUse() || data.getGroupId() > 0;
		final InteractionConstraints constraints = data.getInteractionConstraints();
		final long expirationTimestamp = data.getExpirationTimestamp();

		if (expirationTimestamp > 0 && System.currentTimeMillis() > expirationTimestamp) {
			return new HandleComponentResult(ComponentErrorReason.EXPIRED, true);
		}

		boolean allowed = checkConstraints(event, constraints);

		if (!allowed) {
			return new HandleComponentResult(ComponentErrorReason.NOT_ALLOWED, false);
		}

		return new HandleComponentResult(null, oneUse);
	}

	private boolean checkConstraints(GenericComponentInteractionCreateEvent event, InteractionConstraints constraints) {
		if (constraints.isEmpty()) return true;

		if (constraints.getUserList().contains(event.getUser().getIdLong())) {
			return true;
		}

		final Member member = event.getMember();
		if (member != null) {
			if (!constraints.getPermissions().isEmpty()) {
				if (member.hasPermission(event.getGuildChannel(), constraints.getPermissions())) {
					return true;
				}
			}

			for (Role role : member.getRoles()) {
				boolean hasRole = constraints.getRoleList().contains(role.getIdLong());

				if (hasRole) {
					return true;
				}
			}
		}

		return false;
	}
}
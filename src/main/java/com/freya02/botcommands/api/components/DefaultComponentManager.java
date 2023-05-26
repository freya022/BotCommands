package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.builder.ComponentBuilder;
import com.freya02.botcommands.api.components.builder.LambdaComponentTimeoutInfo;
import com.freya02.botcommands.api.components.builder.PersistentComponentBuilder;
import com.freya02.botcommands.api.components.builder.PersistentComponentTimeoutInfo;
import com.freya02.botcommands.api.components.builder.buttons.LambdaButtonBuilder;
import com.freya02.botcommands.api.components.builder.buttons.PersistentButtonBuilder;
import com.freya02.botcommands.api.components.builder.selects.LambdaSelectionMenuBuilder;
import com.freya02.botcommands.api.components.builder.selects.PersistentSelectionMenuBuilder;
import com.freya02.botcommands.internal.components.HandleComponentResult;
import com.freya02.botcommands.internal.components.data.LambdaButtonData;
import com.freya02.botcommands.internal.components.data.LambdaSelectionMenuData;
import com.freya02.botcommands.internal.components.data.PersistentButtonData;
import com.freya02.botcommands.internal.components.data.PersistentSelectionMenuData;
import com.freya02.botcommands.internal.components.sql.*;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultComponentManager implements ComponentManager {
	private static final Logger LOGGER = Logging.getLogger();
	private static final String LATEST_VERSION = "2";

	private final ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();

	private final Supplier<Connection> connectionSupplier;

	private final Map<Long, ButtonConsumer> buttonLambdaMap = new HashMap<>();
	private final Map<Long, SelectionConsumer<? extends GenericSelectMenuInteractionEvent<?, ?>>> selectionMenuLambdaMap = new HashMap<>();

	public DefaultComponentManager(@NotNull Supplier<Connection> connectionSupplier) {
		this.connectionSupplier = connectionSupplier;

		try {
			setupTables();
		} catch (SQLException e) {
			LOGGER.error("Unable to create DefaultComponentManager", e);

			throw new RuntimeException("Unable to create DefaultComponentManager", e);
		}
	}

	@Override
	@NotNull
	public SQLFetchResult fetchComponent(String id) {
		final Connection connection = getConnection();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"select * " +
							"from componentdata " +
							"left join lambdacomponentdata using (componentid) " +
							"left join persistentcomponentdata using (componentid)" +
							"where componentid = ? " +
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
	public <E extends GenericSelectMenuInteractionEvent<?, ?>> void handleLambdaSelectMenu(GenericComponentInteractionCreateEvent event, FetchResult fetchResult, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData<E>> dataConsumer) {
		//noinspection unchecked
		handleLambdaComponent(event,
				(SQLFetchResult) fetchResult,
				onError,
				dataConsumer,
				(Map<Long, SelectionConsumer<E>>) (Object) selectionMenuLambdaMap, //No choice here really, at least the function will pick a pair with the correct types
				LambdaSelectionMenuData<E>::new);
	}

	private <CONSUMER extends ComponentConsumer<EVENT>,EVENT extends GenericComponentInteractionCreateEvent, DATA> void handleLambdaComponent(GenericComponentInteractionCreateEvent event,
	                                                                                                                                          SQLFetchResult fetchResult,
	                                                                                                                                          Consumer<ComponentErrorReason> onError,
	                                                                                                                                          Consumer<DATA> dataConsumer,
	                                                                                                                                          Map<Long, CONSUMER> map,
	                                                                                                                                          Function<CONSUMER, DATA> eventFunc) {

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

			LOGGER.trace("Created a lambda button with id {}", result.componentId());
			return result.componentId();
		} catch (Exception e) {
			LOGGER.error("An exception occurred while registering a lambda component", e);

			throw new RuntimeException("An exception occurred while registering a lambda component", e);
		}
	}

	@NotNull
	@Override
	public <E extends GenericSelectMenuInteractionEvent<?, ?>> String putLambdaSelectMenu(LambdaSelectionMenuBuilder<?, E> builder) {
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

			LOGGER.trace("Created a lambda select menu with id {}", result.componentId());
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

			if (type == ComponentType.PERSISTENT_BUTTON) {
				LOGGER.trace("Created a persistent component with id {}", componentId);
			} else if (type == ComponentType.PERSISTENT_SELECTION_MENU) {
				LOGGER.trace("Created a lambda button with id {}", componentId);
			}
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

	@NotNull
	@Override
	public <T extends PersistentSelectionMenuBuilder<T>> String putPersistentSelectMenu(T builder) {
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

	private void setupTables() throws SQLException {
		final String setupVersionSql = Utils.readResource("setupVersion.sql");
		try (Connection connection = getConnection();
		     PreparedStatement setupVersionStatement = connection.prepareStatement(
				     setupVersionSql
		     );
		     PreparedStatement readVersionStatement = connection.prepareStatement(
				     "select version from version limit 1;"
		     )) {
			setupVersionStatement.execute();

			final ResultSet set = readVersionStatement.executeQuery();

			if (set.next()) {
				final String currentVersion = set.getString("version");

				if (!currentVersion.equals(LATEST_VERSION)) {
					askUpdate(connection, currentVersion);
				} else {
					LOGGER.trace("Running version {} of the components database", currentVersion);
				}
			} else { //If no version try to see if table exist
				try (PreparedStatement detectDbStatement = connection.prepareStatement("select table_name from information_schema.tables where table_name = 'componentdata' limit 1;")) {
					if (detectDbStatement.executeQuery().next()) {
						askUpdate(connection, "1");
					} else { //No version and no table
						resetTables(connection);

						LOGGER.trace("Running version {} of the components database", LATEST_VERSION);
					}
				}
			}

			final String setupSql = Utils.readResource("setup.sql");

			try (PreparedStatement setupStatement = connection.prepareStatement(setupSql)) {
				setupStatement.execute();
			}
		}
	}

	private void askUpdate(Connection connection, String currentVersion) throws SQLException {
		LOGGER.warn("Database is at version {} but should be at version {}, do you wish to upgrade ?", currentVersion, LATEST_VERSION);
		LOGGER.warn("This will delete all the component tables, other tables are not modified.");
		LOGGER.warn("Enter 'yes' in order to continue, or anything else to abort");

		final Scanner scanner = new Scanner(System.in);
		final String line = scanner.nextLine();

		if (line.equalsIgnoreCase("yes")) {
			resetTables(connection);
		} else {
			LOGGER.error("Database is outdated, aborting");

			throw new IllegalStateException("Database is at version " + currentVersion + " but should be at version " + LATEST_VERSION);
		}
	}

	private void resetTables(Connection connection) throws SQLException {
		try (PreparedStatement resetTablesStatement = connection.prepareStatement(
				Utils.readResource("resetTables.sql")
		)) {
			resetTablesStatement.execute();
		}
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

			if (event.getGuild() != null && constraints.getRoleList().contains(event.getGuild().getIdLong())) {
				return true; //Everyone role
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
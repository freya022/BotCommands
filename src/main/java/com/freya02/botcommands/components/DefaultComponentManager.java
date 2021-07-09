package com.freya02.botcommands.components;

import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.components.builder.*;
import com.freya02.botcommands.components.event.ButtonEvent;
import com.freya02.botcommands.components.event.SelectionEvent;
import com.freya02.botcommands.components.internal.HandleComponentResult;
import com.freya02.botcommands.components.internal.data.LambdaButtonData;
import com.freya02.botcommands.components.internal.data.LambdaSelectionMenuData;
import com.freya02.botcommands.components.internal.data.PersistentButtonData;
import com.freya02.botcommands.components.internal.data.PersistentSelectionMenuData;
import com.freya02.botcommands.components.internal.xodus.XodusComponentData;
import com.freya02.botcommands.components.internal.xodus.XodusLambdaComponentData;
import com.freya02.botcommands.components.internal.xodus.XodusObject;
import com.freya02.botcommands.components.internal.xodus.XodusPersistentComponentData;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.entitystore.*;
import jetbrains.exodus.env.*;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;

public class DefaultComponentManager implements ComponentManager {
	private static final Logger LOGGER = Logging.getLogger();

	private static final String LAMBDA_SELECTION_MENU_DATA_NAME = "LambdaSelectionMenuData";
	private static final String LAMBDA_BUTTON_DATA_NAME = "LambdaButtonData";

	private static final String PERSISTENT_BUTTON_DATA_NAME = "PersistentButtonData";
	private static final String PERSISTENT_SELECTION_MENU_DATA_NAME = "PersistentSelectionMenuData";

	private static final String LAMBDA_BUTTON_SEQUENCE_NAME = "lambdaButtonSequence";
	private static final String LAMBDA_SELECTION_MENU_SEQUENCE_NAME = "lambdaSelectionMenuSequence";

	private static final Map<ComponentType, String> typeToEntityName = Map.of(
			ComponentType.PERSISTENT_BUTTON, PERSISTENT_BUTTON_DATA_NAME,
			ComponentType.LAMBDA_BUTTON, LAMBDA_BUTTON_DATA_NAME,
			ComponentType.PERSISTENT_SELECTION_MENU, PERSISTENT_SELECTION_MENU_DATA_NAME,
			ComponentType.LAMBDA_SELECTION_MENU, LAMBDA_SELECTION_MENU_DATA_NAME
	);

	private final Store idStore;
	private final PersistentEntityStore entityStore;
	private final Environment environment;

	private final Map<Long, Consumer<ButtonEvent>> buttonLambdaMap = new HashMap<>();
	private final Map<Long, Consumer<SelectionEvent>> selectionMenuLambdaMap = new HashMap<>();

	public DefaultComponentManager(Path path) {
		environment = Environments.newInstance(path.toFile());

		idStore = environment.computeInTransaction(txn -> environment.openStore("IdTypeStore", StoreConfig.WITHOUT_DUPLICATES, txn));

		entityStore = PersistentEntityStores.newInstance(environment);

		//Delete lambda IDs from environment
		environment.executeInTransaction(txn -> {
			final Cursor cursor = idStore.openCursor(txn);

			while (cursor.getNext()) {
				final int key = IntegerBinding.entryToInt(cursor.getValue());
				final ComponentType componentType = ComponentType.fromKey(key);

				if (componentType == ComponentType.LAMBDA_BUTTON || componentType == ComponentType.LAMBDA_SELECTION_MENU) {
					cursor.deleteCurrent();
				}
			}
		});

		//Delete lambda button data from entity store
		deleteTemporaryEntities(LAMBDA_BUTTON_DATA_NAME, LAMBDA_SELECTION_MENU_DATA_NAME);

		resetTemporarySequences(LAMBDA_BUTTON_SEQUENCE_NAME, LAMBDA_SELECTION_MENU_SEQUENCE_NAME);
	}

	@Override
	@Nullable
	public ComponentType getIdType(String id) {
		final ComponentType idType = getIdTypeInternal(id);

		if (idType == null) {
			LOGGER.warn("Couldn't find a component with id {}", id);
		}

		return idType;
	}

	@Nullable
	private ComponentType getIdTypeInternal(String id) {
		return environment.computeInReadonlyTransaction(txn -> {
			final ByteIterable keyIterable = idStore.get(txn, StringBinding.stringToEntry(id));

			if (keyIterable == null) {
				return null;
			}

			return ComponentType.fromKey(IntegerBinding.entryToInt(keyIterable));
		});
	}

	@Override
	public void handleLambdaButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaButtonData> dataConsumer) {
		entityStore.executeInTransaction(txn -> {
			final Entity lambdaButtonDataEntity = getComponentDataEntity(txn, LAMBDA_BUTTON_DATA_NAME, "lambda button data", event.getComponentId());
			if (lambdaButtonDataEntity == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final XodusLambdaComponentData xodusLambdaButtonData = XodusObject.read(XodusLambdaComponentData.class, lambdaButtonDataEntity);

			HandleComponentResult handleComponentResult = handleComponentData(event, lambdaButtonDataEntity, xodusLambdaButtonData);

			if (handleComponentResult.getErrorReason() != null) {
				onError.accept(handleComponentResult.getErrorReason());

				return;
			}

			final long handlerId = xodusLambdaButtonData.getHandlerId();

			final Consumer<ButtonEvent> consumer;
			if (handleComponentResult.shouldDelete()) {
				consumer = buttonLambdaMap.remove(handlerId);
			} else {
				consumer = buttonLambdaMap.get(handlerId);
			}

			dataConsumer.accept(new LambdaButtonData(consumer));
		});
	}

	@Override
	public void handleLambdaSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<LambdaSelectionMenuData> dataConsumer) {
		entityStore.executeInTransaction(txn -> {
			final Entity lambdaSelectionMenuDataEntity = getComponentDataEntity(txn, LAMBDA_SELECTION_MENU_DATA_NAME, "lambda selection menu data", event.getComponentId());
			if (lambdaSelectionMenuDataEntity == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final XodusLambdaComponentData xodusLambdaComponentData = XodusObject.read(XodusLambdaComponentData.class, lambdaSelectionMenuDataEntity);

			HandleComponentResult handleComponentResult = handleComponentData(event, lambdaSelectionMenuDataEntity, xodusLambdaComponentData);

			if (handleComponentResult.getErrorReason() != null) {
				onError.accept(handleComponentResult.getErrorReason());

				return;
			}

			final long handlerId = xodusLambdaComponentData.getHandlerId();

			final Consumer<SelectionEvent> consumer;
			if (handleComponentResult.shouldDelete()) {
				consumer = selectionMenuLambdaMap.remove(handlerId);
			} else {
				consumer = selectionMenuLambdaMap.get(handlerId);
			}

			dataConsumer.accept(new LambdaSelectionMenuData(consumer));
		});
	}

	@Override
	public void handlePersistentButton(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentButtonData> dataConsumer) {
		entityStore.executeInTransaction(txn -> {
			final Entity persistentButtonDataEntity = getComponentDataEntity(txn, PERSISTENT_BUTTON_DATA_NAME, "persistent button data", event.getComponentId());
			if (persistentButtonDataEntity == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final XodusPersistentComponentData xodusPersistentComponentData = XodusObject.read(XodusPersistentComponentData.class, persistentButtonDataEntity);

			HandleComponentResult handleComponentResult = handleComponentData(event, persistentButtonDataEntity, xodusPersistentComponentData);

			if (handleComponentResult.getErrorReason() != null) {
				onError.accept(handleComponentResult.getErrorReason());

				return;
			}

			final String handlerName = xodusPersistentComponentData.getHandlerName();
			final List<String> args = xodusPersistentComponentData.getArgs();

			dataConsumer.accept(new PersistentButtonData(handlerName, args));
		});
	}

	@Override
	public void handlePersistentSelectionMenu(GenericComponentInteractionCreateEvent event, Consumer<ComponentErrorReason> onError, Consumer<PersistentSelectionMenuData> dataConsumer) {
		entityStore.executeInTransaction(txn -> {
			final Entity persistentSelectionMenuDataEntity = getComponentDataEntity(txn, PERSISTENT_SELECTION_MENU_DATA_NAME, "persistent selection menu data", event.getComponentId());
			if (persistentSelectionMenuDataEntity == null) {
				onError.accept(ComponentErrorReason.DONT_EXIST);

				return;
			}

			final XodusPersistentComponentData xodusPersistentComponentData = XodusObject.read(XodusPersistentComponentData.class, persistentSelectionMenuDataEntity);

			HandleComponentResult handleComponentResult = handleComponentData(event, persistentSelectionMenuDataEntity, xodusPersistentComponentData);

			if (handleComponentResult.getErrorReason() != null) {
				onError.accept(handleComponentResult.getErrorReason());

				return;
			}

			final String handlerName = xodusPersistentComponentData.getHandlerName();
			final List<String> args = xodusPersistentComponentData.getArgs();

			dataConsumer.accept(new PersistentSelectionMenuData(handlerName, args));
		});
	}

	@Override
	@NotNull
	public String putLambdaButton(LambdaButtonBuilder builder) {
		final String id = entityStore.computeInTransaction(txn -> {
			final long handlerId = txn.getSequence(LAMBDA_BUTTON_SEQUENCE_NAME).increment();
			buttonLambdaMap.put(handlerId, builder.getConsumer());

			final String componentId = generateId();
			final XodusLambdaComponentData lambdaComponentData = new XodusLambdaComponentData(componentId, builder.isOneUse(), builder.getExpirationTimestamp(), builder.getOwnerId(), handlerId);

			XodusObject.write(txn, LAMBDA_BUTTON_DATA_NAME, lambdaComponentData);

			return componentId;
		});

		environment.executeInTransaction(txn -> idStore.put(txn, StringBinding.stringToEntry(id), IntegerBinding.intToEntry(ComponentType.LAMBDA_BUTTON.getKey())));

		return id;
	}

	@Override
	@NotNull
	public String putLambdaSelectionMenu(LambdaSelectionMenuBuilder builder) {
		final String id = entityStore.computeInTransaction(txn -> {
			final long handlerId = txn.getSequence(LAMBDA_SELECTION_MENU_SEQUENCE_NAME).increment();
			selectionMenuLambdaMap.put(handlerId, builder.getConsumer());

			final String componentId = generateId();
			final XodusLambdaComponentData lambdaComponentData = new XodusLambdaComponentData(componentId, builder.isOneUse(), builder.getExpirationTimestamp(), builder.getOwnerId(), handlerId);

			XodusObject.write(txn, LAMBDA_SELECTION_MENU_DATA_NAME, lambdaComponentData);

			return componentId;
		});

		environment.executeInTransaction(txn -> idStore.put(txn, StringBinding.stringToEntry(id), IntegerBinding.intToEntry(ComponentType.LAMBDA_SELECTION_MENU.getKey())));

		return id;
	}

	private <T extends ComponentBuilder<T> & PersistentComponentBuilder> String putPersistentComponent(T builder, String persistentButtonDataName, ComponentType type) {
		final String id = entityStore.computeInTransaction(txn -> {
			final String componentId = generateId();
			final XodusPersistentComponentData persistentComponentData = new XodusPersistentComponentData(componentId,
					builder.isOneUse(),
					builder.getExpirationTimestamp(),
					builder.getOwnerId(),
					builder.getHandlerName(),
					builder.getArgs());

			XodusObject.write(txn, persistentButtonDataName, persistentComponentData);

			return componentId;
		});

		environment.executeInTransaction(txn -> idStore.put(txn, StringBinding.stringToEntry(id), IntegerBinding.intToEntry(type.getKey())));

		return id;
	}

	@Override
	@NotNull
	public String putPersistentButton(PersistentButtonBuilder builder) {
		return putPersistentComponent(builder, PERSISTENT_BUTTON_DATA_NAME, ComponentType.PERSISTENT_BUTTON);
	}

	@Override
	@NotNull
	public String putPersistentSelectionMenu(PersistentSelectionMenuBuilder builder) {
		return putPersistentComponent(builder, PERSISTENT_SELECTION_MENU_DATA_NAME, ComponentType.PERSISTENT_SELECTION_MENU);
	}

	@Override
	public void registerGroup(Collection<String> ids) {
		entityStore.executeInTransaction(txn -> {
			final long groupId = txn.getSequence("groupIdSequence").increment();

			for (String id : ids) {
				final ComponentType idType = getIdTypeInternal(id);

				if (idType == null) {
					LOGGER.trace("No component for id {} while grouping, may not bad if you use your own IDs", id);

					continue;
				}

				final String type = typeToEntityName.get(idType);
				if (type == null) {
					throw new IllegalArgumentException("Unknown ID type: " + idType);
				}

				final Entity entity = getComponentDataEntity(txn, type, "Type '" + type + "'", id);
				if (entity.getRawProperty("groupId") != null) {
					entity.setProperty("groupId", groupId);
				} else {
					LOGGER.error("Found no groupId on entity of type {}", entity.getType());
				}
			}
		});
	}

	private Entity getComponentDataEntity(StoreTransaction txn, String entityType, String name, String id) {
		final EntityIterable entities = txn.find(entityType, "componentId", id);

		if (entities.getRoughCount() > 1) {
			LOGGER.warn("Got more than one {} entity with ID {}", name, id);
		}

		return entities.getFirst();
	}

	private void deleteTemporaryEntities(String... entityTypes) {
		List<String> deletedIds = entityStore.computeInTransaction(txn -> {
			final List<String> ids = new ArrayList<>();

			for (String entityType : entityTypes) {
				int count = 0;

				for (Entity entity : txn.getAll(entityType)) {
					final String componentId = (String) entity.getProperty("componentId");
					if (componentId == null) {
						LOGGER.error("Entity of type {} does not have a component id !", entityType);

						continue;
					}

					XodusObject.deleteRecursively(entity);

					ids.add(componentId);

					count++;
				}

				LOGGER.trace("Deleted {} temporary entities of type {}", count, entityType);
			}

			return ids;
		});

		environment.executeInTransaction(txn -> {
			for (String deletedId : deletedIds) {
				idStore.delete(txn, StringBinding.stringToEntry(deletedId));
			}
		});

		environment.gc();
	}

	private void resetTemporarySequences(String... sequences) {
		entityStore.executeInTransaction(txn -> {
			for (String sequence : sequences) {
				txn.getSequence(sequence).set(0);

				LOGGER.trace("Sequence {} has been reset", sequence);
			}
		});
	}

	private String generateId() {
		return environment.computeInTransaction(txn -> {
			String randomId;

			do {
				randomId = Utils.randomId(64);
			} while (idStore.get(txn, StringBinding.stringToEntry(randomId)) != null);

			return randomId;
		});
	}

	@NotNull
	private HandleComponentResult handleComponentData(GenericComponentInteractionCreateEvent event, Entity complexEntity, @NotNull XodusComponentData xodusComponentData) {
		boolean oneUse = xodusComponentData.isOneUse();
		final long ownerId = xodusComponentData.getOwnerId();
		final long expirationTimestamp = xodusComponentData.getExpirationTimestamp();

		if (expirationTimestamp > 0 && LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) > expirationTimestamp) {
			return new HandleComponentResult(ComponentErrorReason.EXPIRED, true);
		}

		if (ownerId > 0 && event.getUser().getIdLong() != ownerId) {
			return new HandleComponentResult(ComponentErrorReason.NOT_OWNER, false);
		}

		deleteComponent(complexEntity);

		return new HandleComponentResult(null, oneUse);
	}

	@Override
	public int deleteIds(Collection<String> ids) {
		List<ComponentType> types = environment.computeInTransaction(envTxn -> {
			final ArrayList<ComponentType> list = new ArrayList<>();

			final Cursor cursor = idStore.openCursor(envTxn);
			for (String id : ids) {
				//probably more efficient than doing find on every component type
				final ByteIterable entry = cursor.getSearchKey(StringBinding.stringToEntry(id));
				if (entry == null) {
					list.add(null);

					continue;
				}

				list.add(ComponentType.fromKey(IntegerBinding.entryToInt(entry)));

				cursor.deleteCurrent();
			}

			return list;
		});

		return entityStore.computeInTransaction(txn -> {
			int i = 0;
			int deleted = 0;
			for (String id : ids) {
				final ComponentType componentType = types.get(i++);

				if (componentType != null) {
					final String dataName = typeToEntityName.get(componentType);

					for (Entity entity : txn.find(dataName, "componentId", id)) {
						if (entity.delete()) {
							deleted++;
						}
					}
				}
			}

			return deleted;
		});
	}

	private void deleteComponent(Entity complexEntity) {
		final long groupId = getNotNullProperty(complexEntity, "groupId");
		if (groupId >= 0) {
			final StoreTransaction transaction = entityStore.getCurrentTransaction();
			if (transaction != null) {
				environment.executeInTransaction(txn -> {
					for (String entityType : transaction.getEntityTypes()) {
						for (Entity entity : transaction.find(entityType, "groupId", groupId)) {
							entity.delete();

							final ByteIterable componentId = entity.getRawProperty("componentId");
							if (componentId != null) {
								idStore.delete(txn, componentId);
							}
						}
					}
				});
			} else {
				LOGGER.error("StoreTransaction == null");
			}
		} else if ((boolean) getNotNullProperty(complexEntity, "oneUse")) {
			complexEntity.delete();

			final ByteIterable componentId = complexEntity.getRawProperty("componentId");
			if (componentId != null) {
				environment.executeInTransaction(txn -> idStore.delete(txn, componentId));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@NotNull
	private <T> T getNotNullProperty(Entity entity, String propertyName) {
		final Object property = entity.getProperty(propertyName);

		if (property == null) {
			throw new IllegalArgumentException("Expected a property '" + propertyName + "' in entity " + entity.getType());
		}

		return (T) property;
	}
}
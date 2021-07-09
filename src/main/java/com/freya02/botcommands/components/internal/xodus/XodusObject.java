package com.freya02.botcommands.components.internal.xodus;

import com.google.gson.Gson;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityStoreException;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.tables.PropertyTypes;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class XodusObject {
	private static class ClassField {
		private final Class<?> target;
		private final Field field;

		private ClassField(Class<?> target, Field field) {
			this.target = target;
			this.field = field;
		}

		public Class<?> getTarget() {
			return target;
		}

		public Field getField() {
			return field;
		}

		@Override
		public String toString() {
			return "ClassField{" +
					"target=" + target +
					", field=" + field +
					'}';
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(Class<T> clazz, Entity entity) {
		try {
			return (T) readInstance(clazz, entity);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
			throw new RuntimeException("Unable to read entity of type " + entity.getType() + " into an object of type " + clazz.getName(), e);
		}
	}

	public static Entity write(StoreTransaction txn, String entityName, Object instance) {
		try {
			return createEntity(txn, entityName, instance);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException("Unable to write object of type " + instance.getClass().getName() + " into an entity of name " + entityName, e);
		}
	}

	private static Entity createEntity(StoreTransaction txn, String entityName, Object instance) throws IllegalAccessException, NoSuchFieldException {
		final PropertyTypes propertyTypes = ((PersistentEntityStoreImpl) txn.getStore()).getPropertyTypes();
		final Entity entity = txn.newEntity(entityName);

		for (ClassField classField : getFields(instance.getClass(), instance)) {
			final Field field = classField.getField();
			final Object fieldObj = field.get(instance);
			if (fieldObj == null) continue;

			if (List.class.isAssignableFrom(field.getType())) {
				entity.setProperty(field.getName(), new Gson().toJson(fieldObj));
			} else {
				try {
					final Comparable<?> comparable = (Comparable<?>) fieldObj;

					propertyTypes.getPropertyType(comparable.getClass());

					entity.setProperty(field.getName(), comparable);
				} catch (EntityStoreException | ClassCastException ignored) {
					entity.setLink(field.getName(), createEntity(txn, field.getType().getName(), fieldObj));
				}
			}
		}

		return entity;
	}

	@Nonnull
	private static List<ClassField> getFields(Class<?> clazz, Object instance) throws IllegalAccessException {
		final List<ClassField> fields = new ArrayList<>();

		Class<?> target = clazz;
		do {
			for (Field field : target.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;

				if (!field.canAccess(instance) && !field.trySetAccessible()) {
					throw new IllegalAccessException("Cannot access field " + field);
				}

				fields.add(new ClassField(target, field));
			}
		} while(target != (target = clazz.getSuperclass()));

		return fields;
	}

	@Nonnull
	private static Object readInstance(Class<?> clazz, Entity entity) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		final Object instance = getUnsafe().allocateInstance(clazz);

		for (ClassField classField : getFields(instance.getClass(), instance)) {
			final Field field = classField.getField();

			try {
				final Comparable<?> property = entity.getProperty(field.getName());
				if (List.class.isAssignableFrom(field.getType())) {
					final Object o = new Gson().fromJson((String) entity.getProperty(field.getName()), field.getType());

					field.set(instance, o);
				} else if (property != null) {
					field.set(instance, property);
				} else {
					field.set(instance, readInstance(field.getType(), entity.getLink(field.getName())));
				}

				continue;
			} catch (Exception ignored) { }

			//if not set with property then assume link
			field.set(instance, readInstance(field.getType(), entity.getLink(field.getName())));
		}

		return instance;
	}

	private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
		final Field unsafeF = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeF.trySetAccessible();

		return (Unsafe) unsafeF.get(null);
	}

	public static void deleteRecursively(Entity entity) {
		for (String linkName : entity.getLinkNames()) {
			final Entity link = entity.getLink(linkName);
			if (link == null) continue;

			deleteRecursively(link);
		}

		entity.delete();
	}
}

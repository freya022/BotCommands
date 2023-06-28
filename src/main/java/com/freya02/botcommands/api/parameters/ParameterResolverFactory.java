package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.core.utils.ReflectionUtils;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link ParameterResolver parameter resolvers}.
 *
 * @see ParameterResolver
 */
public abstract class ParameterResolverFactory<T extends ParameterResolver<?, R>, R> {
	private final KClass<T> resolverType;
	private final KClass<R> jvmErasure;

	/**
	 * Constructs a new parameter resolver factory
	 *
	 * @param resolverType Class of the resolver
	 * @param clazz Class of the parameter being resolved
	 */
	public ParameterResolverFactory(Class<T> resolverType, @NotNull Class<R> clazz) {
		this.resolverType = ReflectionUtils.toKotlin(resolverType);
		this.jvmErasure = ReflectionUtils.toKotlin(clazz);
	}

	/**
	 * Constructs a new parameter resolver factory
	 *
	 * @param resolverType Class of the resolver
	 * @param clazz Class of the parameter being resolved
	 */
	public ParameterResolverFactory(KClass<T> resolverType, @NotNull KClass<R> clazz) {
		this.resolverType = resolverType;
		this.jvmErasure = clazz;
	}

	public KClass<T> getResolverType() {
		return resolverType;
	}

	public KClass<R> getJvmErasure() {
		return jvmErasure;
	}

	@NotNull
	public abstract T get(@NotNull ParameterWrapper parameter);

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends ParameterResolver<?, R>, R> ParameterResolverFactory<T, R> singleton(T resolver) {
		return new ParameterResolverFactory<>(resolver.getClass(), ReflectionUtils.toJava(resolver.getJvmErasure())) {
			@NotNull
			@Override
			public T get(@NotNull ParameterWrapper parameter) {
				return resolver;
			}
		};
	}
}

package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.EnumResolverBuilder
import io.github.freya022.botcommands.internal.parameters.resolvers.enumerations.EnumResolverBuilderImpl
import io.github.freya022.botcommands.internal.utils.StackSensitive
import io.github.freya022.botcommands.internal.utils.findCaller
import io.github.freya022.botcommands.internal.utils.sourceFile
import java.util.function.Consumer

internal class ResolverManagerImpl internal constructor() : ResolverManager {

    internal val resolverFactories = arrayListOf<ParameterResolverFactory>()
    // I've not added regular resolvers, those are wrapped and have data in their annotations,
    // I believe it is easier to declare them as a class, since they are for simple use cases

    override fun register(resolverFactory: ParameterResolverFactory) {
        resolverFactories += resolverFactory
    }

    override fun <E : Enum<E>> registerEnum(
        enumType: Class<E>,
        block: Consumer<EnumResolverBuilder<E>>,
    ) {
        @OptIn(StackSensitive::class)
        val caller = findCaller()
        val callerSig = "${caller.declaringClass.shortQualifiedName}.${caller.methodName.substringBefore('$')} (${caller.sourceFile}:${caller.lineNumber})"
        register(EnumResolverBuilderImpl(enumType, callerSig).apply(block::accept).build())
    }
}

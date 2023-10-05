package com.freya02.botcommands.test_kt.resolvers

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.test_kt.CustomObject
import net.dv8tion.jda.api.events.Event

@Resolver
class CustomObjectResolver :
    ParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(
        context: BContext,
        executableInteractionInfo: IExecutableInteractionInfo,
        event: Event
    ): CustomObject {
        return CustomObject()
    }
}
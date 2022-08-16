package com.freya02.botcommands.test.resolvers

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.test.CustomObject
import net.dv8tion.jda.api.events.Event

@CommandMarker
class CustomObjectResolver :
    ParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(
        context: BContext,
        executableInteractionInfo: ExecutableInteractionInfo,
        event: Event
    ): CustomObject {
        return CustomObject()
    }
}
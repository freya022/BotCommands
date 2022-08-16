package com.freya02.botcommands.internal.application.context.message

import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.application.context.ContextCommandParameter
import kotlin.reflect.KParameter

class MessageContextCommandParameter(
    parameter: KParameter,
    resolver: MessageContextParameterResolver<*, *>
) : ContextCommandParameter<MessageContextParameterResolver<*, *>>(parameter, resolver)
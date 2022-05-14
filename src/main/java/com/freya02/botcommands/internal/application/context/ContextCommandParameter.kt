package com.freya02.botcommands.internal.application.context

import com.freya02.botcommands.internal.application.ApplicationCommandParameter
import kotlin.reflect.KParameter

class ContextCommandParameter<T>(resolverType: Class<T>, parameter: KParameter, index: Int) :
    ApplicationCommandParameter<T>(resolverType, parameter, index)
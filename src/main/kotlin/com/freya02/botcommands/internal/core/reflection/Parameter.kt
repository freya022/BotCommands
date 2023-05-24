package com.freya02.botcommands.internal.core.reflection

import kotlin.reflect.KParameter

open class Parameter internal constructor(kParameter: KParameter) : KParameter by kParameter {

}

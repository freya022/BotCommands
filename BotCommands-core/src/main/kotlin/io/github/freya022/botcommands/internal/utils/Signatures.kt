package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.utils.getSignature
import kotlin.reflect.KFunction

internal val KFunction<*>.shortSignatureNoSrc: String
    get() = getSignature(returnType = true, source = false)

internal val KFunction<*>.shortSignature: String
    get() = getSignature(returnType = true)
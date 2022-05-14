package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.Logging
import org.slf4j.Logger
import kotlin.reflect.KType

/**
 * Base class for parameter resolvers used in regex commands, application commands and buttons callbacks
 *
 * @see RegexParameterResolver
 * @see QuotableRegexParameterResolver
 * @see ComponentParameterResolver
 * @see SlashParameterResolver
 * @see MessageContextParameterResolver
 * @see UserContextParameterResolver
 */
abstract class ParameterResolver {
    @JvmField
    protected val LOGGER: Logger

    val type: KType

    /**
     * Constructs a new parameter resolver
     *
     * @param type Type of the parameter being resolved
     */
    constructor(type: ParameterType) {
        this.type = type.type
        this.LOGGER = Logging.getLogger(this)
    }
}
package io.github.freya022.botcommands.api.parameters

import javax.annotation.CheckReturnValue

class EnumResolverBuilder<E : Enum<E>> internal constructor(
    val enumType: Class<E>,
    val guildValuesSupplier: EnumValuesSupplier<E>,
) {
    inner class TextSupport(
        val values: Collection<E>,
        val nameFunction: EnumNameFunction<E>,
        val ignoreCase: Boolean,
    ) {
        init {
            require(values.isNotEmpty()) { "Enum values must not be empty" }
        }
    }

    var nameFunction: EnumNameFunction<E> = EnumNameFunction { it.toHumanName() }
        private set
    private var textSupport: TextSupport? = null

    /**
     * Sets the function transforming the enum value into the display name,
     * uses [Resolvers.toHumanName] by default.
     */
    @CheckReturnValue
    @JvmName("setNameFunction")
    fun nameFunction(function: EnumNameFunction<E>) = apply {
        nameFunction = function
    }

    /**
     * Enables resolution of text command parameters.
     *
     * **Note:** Unlike application commands and due to text commands being registered once for everyone,
     * the values cannot be customized per-guild.
     *
     * @param values The values the text option resolver will support
     * @param nameFunction The function returning the display name of the given enum value,
     * this is what the user will need to type
     * @param ignoreCase Whether arguments will be matched while ignoring the casing, `true` by default
     *
     * @throws IllegalArgumentException If [values] is empty.
     */
    @JvmOverloads
    @CheckReturnValue
    fun withTextSupport(values: Collection<E>, nameFunction: EnumNameFunction<E>, ignoreCase: Boolean = true) = apply {
        textSupport = TextSupport(values, nameFunction, ignoreCase)
    }

    @CheckReturnValue
    fun build(): ClassParameterResolver<*, E> {
        val textSupport = textSupport
        return if (textSupport != null) {
            TextEnumResolver(this, textSupport)
        } else {
            EnumResolver(this)
        }
    }
}
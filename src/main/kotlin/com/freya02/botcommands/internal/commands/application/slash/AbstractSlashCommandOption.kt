package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.ApplicationCommandOption
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isJava
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import kotlin.reflect.jvm.jvmErasure

abstract class AbstractSlashCommandOption(
    slashCommandInfo: SlashCommandInfo,
    optionBuilder: SlashCommandOptionBuilder,
    final override val resolver: SlashParameterResolver<*, *>
) : ApplicationCommandOption(optionBuilder) {
    val varArgs = optionBuilder.varArgs
    private val numRequired = optionBuilder.requiredVarArgs
    val isVarArg: Boolean
        get() = varArgs != -1

    init {
        if (isVarArg) {
            requireUser(optionBuilder.type.jvmErasure == List::class) {
                "Type of vararg options should be List"
            }

            if (varArgs != numRequired) {
                if (optionBuilder.type.collectionElementType?.isMarkedNullable == false && !slashCommandInfo.method.isJava) {
                    throwUser(slashCommandInfo.method, "List element type should be nullable on vararg parameters where not all arguments are required")
                }
            }
        }
    }

    fun isRequiredVararg(varArgNum: Int): Boolean {
        if (!isVarArg) throwInternal("Method should have not been used outside of vararg options")
        return varArgNum < numRequired
    }
}
package dev.freya02.botcommands.jda.ktx.messages

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * @see MessageEditData.fromCreateData
 */
@DeprecatedInBcCore
fun MessageCreateData.toEditData(): MessageEditData =
    MessageEditData.fromCreateData(this)

/**
 * @see MessageCreateData.fromEditData
 */
@DeprecatedInBcCore
fun MessageEditData.toCreateData(): MessageCreateData =
    MessageCreateData.fromEditData(this)

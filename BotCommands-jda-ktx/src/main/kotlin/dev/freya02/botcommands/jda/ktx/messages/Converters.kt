package dev.freya02.botcommands.jda.ktx.messages

import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * @see MessageEditData.fromCreateData
 */
fun MessageCreateData.toEditData(): MessageEditData =
    MessageEditData.fromCreateData(this)

/**
 * @see MessageCreateData.fromEditData
 */
fun MessageEditData.toCreateData(): MessageCreateData =
    MessageCreateData.fromEditData(this)

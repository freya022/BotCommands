package io.github.freya022.botcommands.api.modals

import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

fun Modals.create(title: String, block: context(Modals) ModalBuilder.() -> Unit) =
    create(title).apply { block(this@create, this) }.build()

context(Modals)
fun ModalBuilder.textInput(inputName: String, label: String, inputStyle: TextInputStyle, block: TextInputBuilder.() -> Unit = {}) {
    addActionRow(createTextInput(inputName, label, inputStyle).apply(block).build())
}

context(Modals)
fun ModalBuilder.shortTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}) {
    textInput(inputName, label, TextInputStyle.SHORT, block)
}

context(Modals)
fun ModalBuilder.paragraphTextInput(inputName: String, label: String, block: TextInputBuilder.() -> Unit = {}) {
    textInput(inputName, label, TextInputStyle.PARAGRAPH, block)
}
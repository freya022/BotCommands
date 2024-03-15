package io.github.freya022.botcommands.api.pagination.menu.buttonized

import io.github.freya022.botcommands.api.utils.ButtonContent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

data class StyledButtonContent(val style: ButtonStyle, val content: ButtonContent)
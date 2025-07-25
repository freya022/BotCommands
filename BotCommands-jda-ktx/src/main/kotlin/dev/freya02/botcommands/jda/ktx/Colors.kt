package dev.freya02.botcommands.jda.ktx

import org.intellij.lang.annotations.Language
import java.awt.Color

/**
 * Converts the provided Red-Green-Blue color components into an integer.
 */
fun rgb(red: Int, green: Int, blue: Int): Int = Color(red, green, blue).rgb

/**
 * Converts the provided Hue-Saturation-Brightness color components into an integer.
 */
fun hsb(hue: Float, saturation: Float, brightness: Float): Int = Color.HSBtoRGB(hue, saturation, brightness)

/**
 * Converts the provided hexadecimal color into an integer.
 */
fun hex(@Language(value = "html", prefix = "<div style=\"border-left: #", suffix = "\"/>") hex: String): Int =
    hex.hexToInt()

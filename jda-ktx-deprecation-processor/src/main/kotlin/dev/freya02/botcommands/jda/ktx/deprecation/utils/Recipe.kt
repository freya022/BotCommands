package dev.freya02.botcommands.jda.ktx.deprecation.utils

import dev.freya02.botcommands.jda.ktx.deprecation.FindReplacePairs

fun createFindAndReplaceRecipe(name: String, description: String, pairs: FindReplacePairs): String {
    val header = """
        ---
        type: specs.openrewrite.org/v1beta/recipe
        name: $name
        description: $description
        recipeList:
    """.trimIndent()

    val recipes = pairs.pairs.entries.joinToString("\n") { (old, replacements) ->
        """
            - org.openrewrite.text.FindAndReplace:
                find: "import $old"
                replace: "${replacements.joinToString("\\n") { "import $it" }}"
            - org.openrewrite.text.FindAndReplace:
                find: "import ${old.getPackage()}.*"
                replace: "${replacements.joinToString("\\n") { "import ${it.getPackage()}.*" }}"
        """.trimIndent().prependIndent("  ")
    }

    return header + "\n" + recipes
}

private fun String.getPackage(): String {
    // Assume there are no rule with nested classes, so we can just drop the last import component
    return substringBeforeLast('.')
}

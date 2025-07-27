package dev.freya02.botcommands.jda.ktx.deprecation.utils

import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace

fun createFindAndReplaceRecipe(name: String, description: String, pairs: Collection<RewriteFindReplace>): String {
    val header = """
        ---
        type: specs.openrewrite.org/v1beta/recipe
        name: $name
        description: $description
        recipeList:
    """.trimIndent()

    val recipes = pairs.withStarImports().joinToString("\n") { (_, old, new) ->
        """
            - org.openrewrite.text.FindAndReplace:
                find: "$old"
                replace: "$new"
        """.trimIndent().prependIndent("  ")
    }

    return header + "\n" + recipes
}

private fun Collection<RewriteFindReplace>.withStarImports(): List<RewriteFindReplace> {
    val added = hashSetOf<Pair<String, String>>()

    return flatMap { rule ->
        fun String.getPackage(): String {
            // Assume there are no rule with nested classes, so we can just drop the last import component
            return substringBeforeLast('.')
        }

        val oldPackage = rule.old.getPackage()
        val newPackage = rule.new.getPackage()
        if (added.add(oldPackage to newPackage)) {
            listOf(rule, rule.copy(old = "$oldPackage.*", new = "$oldPackage.*\\nimport $newPackage.*"))
        } else {
            listOf(rule)
        }
    }
}

package io.github.freya022.botcommands.oldtests

import kotlin.io.path.Path
import kotlin.io.path.readText

object Diff {
    @JvmStatic
    fun main(args: Array<String>) {
        val diff = Path("C:\\Users\\freya02\\Downloads\\Rename_com_freya02_to_io_github_freya022.patch").readText()

        lateinit var file: String
        var newFile = false
        val reported = hashSetOf<String>()
        diff.lines().forEach { line ->
            if (line.startsWith("diff --git a/")) {
                file = line.substringAfter("diff --git a/").substringBefore(' ')
                newFile = false
            } else if (line.startsWith("new file mode")) {
                newFile = true
            } else if (line.startsWith("+ ") && !newFile) {
                val code = line.substringAfter("+ ").trim()
                if (!code.startsWith("import") && !code.startsWith("package")) {
                    if (reported.add(file)) {
                        val name = file.substringAfterLast('/')
                        System.err.println("at io.github.freya022.botcommands.${name.substringBefore('.')}($name:0)")
                    }
                }
            }
        }
    }
}
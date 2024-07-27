package io.github.freya022.botcommands.internal.commands.application.diff

import io.github.freya022.botcommands.internal.application.diff.DiffLogger
import io.github.freya022.botcommands.internal.application.diff.logDifferent
import io.github.freya022.botcommands.internal.application.diff.logSame

internal object OldApplicationCommandDiffEngine : ApplicationCommandDiffEngine {
    context(DiffLogger)
    override fun checkCommands(oldCommands: List<Map<String, *>>, newCommands: List<Map<String, *>>): Boolean {
        return checkDiff(oldCommands, newCommands, indent = 0)
    }

    context(DiffLogger)
    private fun checkDiff(oldObj: Any?, newObj: Any?, indent: Int): Boolean {
        if (oldObj == null && newObj == null) {
            return true
        }

        if (oldObj == null) {
            return logDifferent(indent) { "oldObj is null" }
        } else if (newObj == null) {
            return logDifferent(indent) { "newObj is null" }
        }

        if (oldObj.javaClass != newObj.javaClass) {
            return logDifferent(indent) { "Class type not equal: ${oldObj.javaClass.simpleName} to ${newObj.javaClass.simpleName}" }
        }

        return if (oldObj is Map<*, *> && newObj is Map<*, *>) {
            checkMap(oldObj, newObj, indent)
        } else if (oldObj is List<*> && newObj is List<*>) {
            checkList(oldObj, newObj, indent)
        } else {
            (oldObj == newObj).also { equals ->
                if (!equals) logDifferent(indent) { "Not same object: $oldObj to $newObj" }
            }
        }
    }

    context(DiffLogger)
    private fun checkList(oldList: List<*>, newList: List<*>, indent: Int): Boolean {
        if (oldList.size != newList.size) return false

        for (i in oldList.indices) {
            var found = false
            var index = -1
            for (o in newList) {
                index++
                if (checkDiff(oldList[i], o, indent + 1)) {
                    found = true
                    break
                }
            }

            if (found) {
                //If command options (parameters, not subcommands, not groups) are moved
                // then it means the command data changed
                if (i != index) {
                    //Check if any final command property is here,
                    // such as autocomplete, or required
                    if (oldList[index] is Map<*, *>) {
                        val map = oldList[index] as Map<*, *>
                        if (map["autocomplete"] != null) {
                            //We found a real command option that has **changed index**,
                            // this is NOT equal under different indexes
                            return logDifferent(indent) {
                                "Final command option has changed place from index $i to $index : ${oldList[i]}"
                            }
                        }
                    }
                }

                logSame(indent) { "Found exact object at index $index (original object at $i) : ${oldList[i]}" }
                continue
            }

            if (!checkDiff(oldList[i], newList[i], indent + 1)) {
                return logDifferent(indent) { "List item not equal: ${oldList[i]} to ${newList[i]}" }
            }
        }

        return true
    }

    context(DiffLogger)
    private fun checkMap(oldMap: Map<*, *>, newMap: Map<*, *>, indent: Int): Boolean {
        if (!oldMap.keys.containsAll(newMap.keys)) return false

        for (key in oldMap.keys) {
            if (!checkDiff(oldMap[key], newMap[key], indent + 1)) {
                return logDifferent(indent) { "Map value not equal for key 'key': ${oldMap[key]} to ${newMap[key]}" }
            }
        }

        return true
    }
}
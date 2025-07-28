package dev.freya02.botcommands.jda.ktx.deprecation

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile

class FindReplacePairs {

    private val _processedFiles = mutableListOf<KSFile>()
    val processedFiles: List<KSFile> get() = _processedFiles

    private val _pairs = sortedMapOf<String, MutableSet<String>>()
    val pairs: Map<String, Set<String>> get() = _pairs

    fun add(symbol: KSAnnotated, old: String, new: String) {
        _processedFiles.add(symbol.containingFile!!)
        _pairs.computeIfAbsent(old) { sortedSetOf() }.add(new)
    }

    fun add(pair: RewriteFindReplace) {
        _processedFiles.add(pair.processedFile)
        _pairs.computeIfAbsent(pair.old) { sortedSetOf() }.add(pair.new)
    }
}

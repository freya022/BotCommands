package dev.freya02.botcommands.restarter.internal.utils

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

// Optimization of Path#walk, cuts CPU usage by 4
// mostly by eliminating duplicate calls to file attributes
internal fun Path.walkFiles(): List<Pair<Path, BasicFileAttributes>> {
    return buildList {
        Files.walkFileTree(this@walkFiles, object : FileVisitor<Path> {
            override fun preVisitDirectory(
                dir: Path,
                attrs: BasicFileAttributes
            ): FileVisitResult = FileVisitResult.CONTINUE

            override fun visitFile(
                file: Path,
                attrs: BasicFileAttributes
            ): FileVisitResult {
                add(file to attrs)
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(
                file: Path,
                exc: IOException
            ): FileVisitResult = FileVisitResult.CONTINUE

            override fun postVisitDirectory(
                dir: Path,
                exc: IOException?
            ): FileVisitResult = FileVisitResult.CONTINUE
        })
    }
}

internal fun Path.walkDirectories(block: (Path, BasicFileAttributes) -> Unit) {
    Files.walkFileTree(this@walkDirectories, object : FileVisitor<Path> {
        override fun preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult {
            block(dir, attrs)
            return FileVisitResult.CONTINUE
        }

        override fun visitFile(
            file: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = FileVisitResult.CONTINUE

        override fun visitFileFailed(
            file: Path,
            exc: IOException
        ): FileVisitResult = FileVisitResult.CONTINUE

        override fun postVisitDirectory(
            dir: Path,
            exc: IOException?
        ): FileVisitResult = FileVisitResult.CONTINUE
    })
}

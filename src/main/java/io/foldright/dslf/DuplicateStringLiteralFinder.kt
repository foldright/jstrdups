package io.foldright.dslf

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17
import com.github.javaparser.Position
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.utils.ParserCollectionStrategy
import com.github.javaparser.utils.SourceRoot
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.system.exitProcess


@Command(
    name = "jstrdups", version = ["0.1.0"],
    description = ["Find duplicate string literals in java files under current directory"],
    mixinStandardHelpOptions = true
)
class DuplicateStringLiteralFinder : Runnable {
    @Parameters(index = "0", defaultValue = ".", description = ["Project root dir, default is the current directory"])
    lateinit var projectRootDir: Path

    @Option(names = ["--include-test-dir", "-t"], description = ["include test dir, default is false"])
    var includeTestDir: Boolean = false

    @Option(
        names = ["--absolute-path", "-a"],
        description = ["always print the absolute path of java files, default is false"]
    )
    var absolutePath: Boolean = false

    @Option(
        names = ["--min-string-len", "-l"],
        description = ["minimal string length(char count) of string literal to find, default is 4"]
    )
    var minStrLen: Int = 4

    @Option(
        names = ["--min-duplicate-count", "-d"],
        description = ["minimal duplicate count of string literal to find"]
    )
    var minDuplicateCount: Int = 2

    @Option(names = ["--verbose", "-v"], description = ["print messages about progress"])
    var verbose: Boolean = false

    override fun run() {
        val compilationUnitList = collectCompilationUnits(
            projectRootDir.toNormalizePath(), includeTestDir, verbose
        )
        compilationUnitList.findDuplicateStrLiteralInfos(minStrLen, minDuplicateCount)
            .print(absolutePath)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            exitProcess(CommandLine(DuplicateStringLiteralFinder()).execute(*args))
        }
    }
}

private fun Path.toNormalizePath(): Path = absolute().normalize()

private fun collectCompilationUnits(
    projectRootPath: Path, includeTestDir: Boolean, verbose: Boolean
): List<CompilationUnit> = ParserCollectionStrategy(ParserConfiguration().setLanguageLevel(JAVA_17))
    .collect(projectRootPath)
    .sourceRoots
    .filter { sourceRoot: SourceRoot ->
        val root: Path = sourceRoot.root
        val result = includeTestDir || !root.endsWith("test/java")
        result.also {
            if (!verbose) return@also
            if (it) println("found  source root: $root")
            else println("ignore source root: $root")
        }
    }
    .flatMap { it.tryToParseParallelized() }
    .filter { it.result.isPresent }
    .map { it.result.get() }

private fun List<CompilationUnit>.findDuplicateStrLiteralInfos(
    minStrLen: Int, minDuplicateCount: Int
): List<GroupStrLiterals> = flatMap { it.findAllStringLiterals(minStrLen) }
    .groupBy { it.value }
    .filter { it.value.size >= minDuplicateCount }
    .map { (key, value) -> GroupStrLiterals(key, value) }
    .sortedByDescending { it.strLiterals.size }

private fun CompilationUnit.findAllStringLiterals(minLen: Int): List<StrLiteral> =
    findAll(StringLiteralExpr::class.java)
        .filter { it.value.length >= minLen }
        .map { StrLiteral(it.value, it, this) }

private fun List<GroupStrLiterals>.print(absolutePath: Boolean) {
    val inJetBrainsIde: Boolean = System.getenv("TERMINAL_EMULATOR")?.contains("JetBrains", true) ?: false

    forEachIndexed { index, (v, infoList) ->
        System.out.printf(
            "[%3d/%3d](count: %2d) duplicate string literal \"%s\" at%n",
            index + 1, size, infoList.size, v
        )
        infoList.forEachIndexed { idx, (_, expr, cu) ->
            val p: Position = expr.begin.get()
            when {
                absolutePath -> System.out.printf(
                    "  [%2d/%2d] %s:%s:%s%n",
                    idx + 1, infoList.size, cu.storage.get().path.toNormalizePath(), p.line, p.column
                )

                inJetBrainsIde -> System.out.printf(
                    "  [%2d/%2d] .(%s:%s):%s%n",
                    idx + 1, infoList.size, cu.storage.get().path.fileName, p.line, p.column
                )

                else -> System.out.printf(
                    "  [%2d/%2d] %s:%s:%s%n",
                    idx + 1, infoList.size, cu.storage.get().path.fileName, p.line, p.column
                )
            }
        }
    }
}

/**
 * String Literals with same string value
 */
data class GroupStrLiterals(
    val value: String,
    val strLiterals: List<StrLiteral>
)

/**
 * String Literal, contains related infos.
 */
data class StrLiteral(
    val value: String,
    val expr: StringLiteralExpr,
    val cu: CompilationUnit
)

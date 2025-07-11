package io.foldright.dslf

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ParserConfiguration.LanguageLevel
import com.github.javaparser.Position
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.utils.ParserCollectionStrategy
import com.github.javaparser.utils.SourceRoot
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.relativeTo
import kotlin.system.exitProcess


@Command(
    name = "jstrdups", version = ["0.3.0-SNAPSHOT"],
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
        description = ["minimal duplicate count of string literal to find, default is 2"]
    )
    var minDuplicateCount: Int = 2

    @Option(
        names = ["--java-lang-level", "-L"], description = ["set java language level of input java sources."
                + $$" Valid keys: ${COMPLETION-CANDIDATES}. default is JAVA_21"]
    )
    var javaLanguageLevel: LanguageLevel = LanguageLevel.JAVA_21

    @Option(names = ["--verbose", "-v"], description = ["print messages about progress"])
    var verbose: Boolean = false

    override fun run() {
        val compilationUnitList = collectCompilationUnits(
            projectRootDir.normalizedAbsPath(), includeTestDir, javaLanguageLevel, verbose
        )
        compilationUnitList.findDuplicateStrLiteralInfos(minStrLen, minDuplicateCount)
            .print(projectRootDir, absolutePath)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            exitProcess(CommandLine(DuplicateStringLiteralFinder()).execute(*args))
        }
    }
}

private fun collectCompilationUnits(
    projectRootPath: Path, includeTestDir: Boolean, javaLanguageLevel: LanguageLevel, verbose: Boolean
): List<CompilationUnit> = ParserCollectionStrategy(ParserConfiguration().setLanguageLevel(javaLanguageLevel))
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

private fun List<GroupStrLiterals>.print(projectRootDir: Path, absolutePath: Boolean) {
    val inJetBrainsIde: Boolean = System.getenv("TERMINAL_EMULATOR")
        ?.contains("JetBrains", ignoreCase = true) ?: false

    val groupCountWidth = this.count().toString().length
    val maxDupCountWidth = this.maxOfOrNull { it.strLiterals.size.toString().length } ?: 1

    forEachIndexed { index, (v, infoList) ->
        System.out.printf(
            "[%${groupCountWidth}s/%s](count: %${maxDupCountWidth}s) duplicate string literal \"%s\" at%n",
            index + 1, size, infoList.size, v
        )
        infoList.forEachIndexed { idx, (_, expr, cu) ->
            val p: Position = expr.begin.get()
            val indicator = String.format("  [%${maxDupCountWidth}s/%${maxDupCountWidth}s] ", idx + 1, infoList.size)
            when {
                absolutePath -> System.out.printf(
                    "%s%s:%s:%s%n",
                    indicator, cu.storage.get().path.normalizedAbsPath(), p.line, p.column
                )

                inJetBrainsIde -> System.out.printf(
                    "%s.(%s:%s):%s%n",
                    indicator, cu.storage.get().path, p.line, p.column
                )

                else -> System.out.printf(
                    "%s%s:%s:%s%n",
                    indicator, cu.storage.get().path.alignTo(projectRootDir), p.line, p.column
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

internal fun Path.normalizedAbsPath(): Path = absolute().normalize()

internal fun Path.alignTo(aim: Path): Path =
    if (aim.isAbsolute) normalizedAbsPath()
    else absolute().relativeTo(aim.absolute()).normalize()

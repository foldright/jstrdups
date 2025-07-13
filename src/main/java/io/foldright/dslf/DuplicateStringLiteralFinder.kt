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
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.relativeTo
import kotlin.system.exitProcess


@Command(
    name = "jstrdups", version = ["0.3.0-SNAPSHOT"],
    description = ["Find duplicate string literals in java files under current directory"],
    mixinStandardHelpOptions = true
)
private class DuplicateStringLiteralFinder : Runnable {
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
        compilationUnitList.findDuplicateStrLiterals(minStrLen, minDuplicateCount)
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
    projectRootPath: Path, includeTestDir: Boolean, langLevel: LanguageLevel, verbose: Boolean
): List<CompilationUnit> = ParserCollectionStrategy(ParserConfiguration().setLanguageLevel(langLevel))
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

private fun List<CompilationUnit>.findDuplicateStrLiterals(
    minStrLen: Int, minDuplicateCount: Int
): List<GroupStrLiterals> = flatMap { it.findAllStringLiterals(minStrLen) }
    .groupBy { it.expr.value }
    .filter { it.value.size >= minDuplicateCount }
    .map { (key, value) -> GroupStrLiterals(key, value) }
    .sortedByDescending { it.strLiterals.size }

private fun CompilationUnit.findAllStringLiterals(minLen: Int): List<StrLiteral> =
    findAll(StringLiteralExpr::class.java)
        .filter { it.value.length >= minLen }
        .map { StrLiteral(it, this) }

private fun List<GroupStrLiterals>.print(projectRootDir: Path, absolutePath: Boolean) {
    val inJetBrainsIde: Boolean = System.getenv("TERMINAL_EMULATOR")
        ?.contains("JetBrains", ignoreCase = true) ?: false

    val groupCountWidth = this.size.toString().length
    val dupCountWidth = this.maxOfOrNull { it.strLiterals.size.toString().length } ?: 1

    forEachIndexed { index, (v, strLiterals) ->
        printf(
            "[%${groupCountWidth}s/%s](count: %${dupCountWidth}s) duplicate string literal \"%s\" at%n",
            index + 1, size, strLiterals.size, v
        )
        strLiterals.forEachIndexed { idx, (expr, cu) ->
            val p: Position = expr.begin.get()
            val indicator = String.format("  [%${dupCountWidth}s/%${dupCountWidth}s] ", idx + 1, strLiterals.size)
            val cuPath: Path = cu.storage.get().path
            when {
                absolutePath -> printf("%s%s:%s:%s%n", indicator, cuPath.normalizedAbsPath(), p.line, p.column)
                inJetBrainsIde -> printf("%s.(%s:%s):%s%n", indicator, cuPath.fileName, p.line, p.column)
                else -> printf("%s%s:%s:%s%n", indicator, cuPath.alignTo(projectRootDir), p.line, p.column)
            }
        }
    }
}

/**
 * String Literals with same string value
 */
private data class GroupStrLiterals(val value: String, val strLiterals: List<StrLiteral>)

/**
 * String Literal, contains related infos.
 */
private data class StrLiteral(val expr: StringLiteralExpr, val cu: CompilationUnit)

private fun Path.normalizedAbsPath(): Path = absolute().normalize()

/**
 * Returns a path that matches the absolute/relative form of the target path.
 */
internal fun Path.alignTo(target: Path): Path =
    if (target.isAbsolute) normalizedAbsPath()
    else absolute().relativeTo(target.absolute()).normalize()

private fun printf(format: String, vararg args: Any): PrintStream = System.out.printf(format, *args)

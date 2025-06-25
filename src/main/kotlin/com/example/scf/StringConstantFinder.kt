@file:JvmName("StringConstantFinder")

package com.example.scf

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17
import com.github.javaparser.Position
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.utils.ParserCollectionStrategy
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute


fun main(args: Array<String>) {
    if (args.size != 1) throw IllegalArgumentException("only one argument project root")
    val projectRootPath: Path = args[0].toNormalizePath()
    val compilationUnitList = collectCompilationUnits(projectRootPath)
    compilationUnitList.findDuplicateStrLiteralInfos().print()
}

private fun String.toNormalizePath(): Path = Paths.get(this).absolute().normalize()

private fun collectCompilationUnits(projectRootPath: Path, excludeTestDir: Boolean = true): List<CompilationUnit> =
    ParserCollectionStrategy(ParserConfiguration().setLanguageLevel(JAVA_17))
        .collect(projectRootPath)
        .sourceRoots
        .onEach { println("found ${it.root}") }
        // ℹ️ 不处理测试代码
        .filterNot { excludeTestDir && it.root.endsWith("/src/test/java") }
        .flatMap { it.tryToParseParallelized() }
        .filter { it.result.isPresent }
        .map { it.result.get() }

private fun List<CompilationUnit>.findDuplicateStrLiteralInfos(
    duplicateThreshold: Int = 2
): List<GroupStrLiterals> = flatMap { it.findAllStringLiterals() }
    .groupBy { it.value }
    // ℹ️ 过滤没有重复使用的字符串常量（只用了一次）
    .filter { it.value.size >= duplicateThreshold }
    .map { (key, value) -> GroupStrLiterals(key, value) }
    // 按常量的重复次数倒排
    .sortedByDescending { it.strLiterals.size }

private fun CompilationUnit.findAllStringLiterals(minLen: Int = 4): List<StrLiteral> =
    findAll(StringLiteralExpr::class.java)
        .filter { it.value.length >= minLen }
        .map { StrLiteral(it.value, it, this) }

private fun List<GroupStrLiterals>.print() {
    forEachIndexed { index, (v, infoList) ->
        System.out.printf(
            "[%3d/%3d](count: %2d) duplicate string literal \"%s\" at%n",
            index + 1, size, infoList.size, v
        )
        infoList.forEachIndexed { idx, (_, expr, cu) ->
            val p: Position = expr.begin.get()
            System.out.printf(
                "  [%2d/%2d] .(%s:%s):%s%n",
                idx + 1, infoList.size, cu.storage.get().path.fileName, p.line, p.column
            )
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
    val stringLiteralExpr: StringLiteralExpr,
    val cu: CompilationUnit
)

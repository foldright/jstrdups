@file:JvmName("StringConstantFinder")

package com.example.scf

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17
import com.github.javaparser.Position
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.utils.ParserCollectionStrategy
import java.nio.file.Paths


fun main(args: Array<String>) {
    if (args.size != 1) throw IllegalArgumentException("only one argument project root")

    val projRootPath = args[0]
    val projectRoot = ParserCollectionStrategy(ParserConfiguration().setLanguageLevel(JAVA_17))
        .collect(Paths.get(projRootPath))

    val compilationUnitList: List<CompilationUnit> = projectRoot.sourceRoots
        .onEach { println("found ${it.root}") }
        // ℹ️ 不处理测试代码
        .filterNot { it.root.endsWith("/src/test/java") }
        .flatMap { it.tryToParseParallelized() }
        .filter {
            if (!it.isSuccessful) println(it.problems)
            it.isSuccessful
        }
        .map { it.result.get() }

    val duplicateStrLiteralInfos: List<Pair<String, List<StringLiteralInfo>>> = compilationUnitList
        .flatMap { cu ->
            cu.findAll(StringLiteralExpr::class.java)
                // ℹ️ 不找 短的字符串常量
                .filter { it.value.length >= 4 }
                .map { StringLiteralInfo(it.value, it, cu) }
        }
        .groupBy { it.value }
        // ℹ️ 过滤没有重复使用的字符串常量（只用了一次）
        .filter { it.value.size > 1 }
        .map { (key, value) -> key to value }
        // 按常量的重复次数倒排
        .sortedByDescending { it.second.size }

    printStringLiteralInfo(duplicateStrLiteralInfos)
}

fun printStringLiteralInfo(duplicateStrLiteralInfos: List<Pair<String, List<StringLiteralInfo>>>) {
    duplicateStrLiteralInfos.forEachIndexed { index, (v, infoList) ->
        System.out.printf(
            "[%3d/%3d](count: %2d) duplicate string literal \"%s\" at%n",
            index + 1, duplicateStrLiteralInfos.size, infoList.size, v
        )
        infoList.forEachIndexed { idx, (_, expr, cu) ->
            val p: Position = expr.begin.get()
            System.out.printf(
                "  [%2d/%2d] %s:%s:%s%n",
                idx + 1, infoList.size, cu.storage.get().path.fileName, p.line, p.column
            )
        }
    }
}

data class StringLiteralInfo(
    val value: String,
    val stringLiteralExpr: StringLiteralExpr,
    val cu: CompilationUnit
)


val BLACK_LIST = setOf(
    "true", "null",
    "body", "header", "etag",
    "type", "version",
    "unknown", "staging"
)

@Suppress("unused")
val filter: (String) -> Boolean = {
    !BLACK_LIST.contains(it) && it.length > 1
}

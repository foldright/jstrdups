package io.foldright.dslf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute

class DuplicateStringLiteralFinderTest : FunSpec({
    val absWorkDir: Path = p().absolute()

    test("alignTo") {
        val pc = p("$absWorkDir", "c")

        pc.alignTo(absWorkDir).toString() shouldBe pc.toString()

        p("c").alignTo(absWorkDir).toString() shouldBe p("", "c").absolute().toString()
        p("$absWorkDir", "c", "..", "d").alignTo(absWorkDir).toString() shouldBe p("", "d").absolute().toString()

        p("c").alignTo(p(".")).toString() shouldBe "c"
        p("c").alignTo(p("")).toString() shouldBe "c"
    }

    test("java.nio.file.Path usage") {
        p() shouldBe p(".").normalize()
    }
})

private fun p(first: String = "", vararg more: String): Path = Paths.get(first, *more)
private val String.p: Path get() = Paths.get(this)

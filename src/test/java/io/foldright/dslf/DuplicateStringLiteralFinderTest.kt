package io.foldright.dslf

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths
import kotlin.io.path.absolute

class DuplicateStringLiteralFinderTest : FunSpec({
    test("alignTo") {
        p("/a/b/c").alignTo(p("/a/b/")).toString() shouldBe "/a/b/c"
        p("c").alignTo(p("/a/b/")).toString() shouldBe "${p().absolute()}/c"

        p("c").alignTo(p(".")).toString() shouldBe "c"
        p("c").alignTo(p("")).toString() shouldBe "c"
    }

    test("java.nio.file.Path usage") {
        p() shouldBe p(".").normalize()
    }
})

private fun p(path: String = "") = Paths.get(path)

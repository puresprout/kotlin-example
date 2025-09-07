package com.purestation.app.kotest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay

// INFO IDEA내에서 테스트를 실행하려면 kotest 플러그인을 설치해야 함
class StringSpecSample : StringSpec({
    "문자열 길이는 기대값과 같아야 한다" {
        "kotest".length shouldBe 6
    }
})



class FunSpecSample : FunSpec({
    test("덧셈 결과는 양수다") {
        (2 + 3) shouldBeGreaterThan 0
    }
})



class BehaviorSpecSample : BehaviorSpec({
    Given("두 수가 주어졌을 때") {
        val a = 2; val b = 3
        When("더하면") {
            val sum = a + b
            Then("합은 5여야 한다") {
                sum shouldBe 5
            }
        }
    }
})



class DescribeSpecSample : DescribeSpec({
    describe("가변 리스트") {
        val list = mutableListOf<Int>()
        context("요소 추가") {
            it("push 후 포함해야 한다") {
                list.add(10)
                list shouldContain 10
            }
        }
    }
})



fun boom(): Nothing = throw IllegalArgumentException("bad input")

class ExceptionSample : FunSpec({
    test("예외와 메시지 확인") {
        val e = shouldThrow<IllegalArgumentException> { boom() }
        e.message.shouldContain("bad")
    }
})



class DataTestSample : StringSpec({
    "최대값 계산" {
        forAll(
            row(1, 3, 3),
            row(-2, -5, -2),
            row(10, 10, 10),
        ) { a, b, expected ->
            maxOf(a, b) shouldBe expected
        }
    }
})



class CoroutineSample : FunSpec({
    test("suspend 함수 결과 검증") {
        suspend fun fetch(): Int { delay(10); return 42 }
        fetch() shouldBe 42
    }
})

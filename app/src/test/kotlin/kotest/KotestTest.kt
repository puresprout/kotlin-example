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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

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



class CalculatorTest : FunSpec({
    class Calculator {
        fun add(a: Int, b: Int) = a + b
        fun divide(a: Int, b: Int): Int {
            require(b != 0) { "b must not be zero" }
            return a / b
        }
    }

    val calc = Calculator()

    test("add는 두 수의 합을 반환한다") {
        calc.add(2, 3) shouldBe 5
    }

    test("divide는 0으로 나누면 IllegalArgumentException을 던진다") {
        shouldThrow<IllegalArgumentException> {
            calc.divide(10, 0)
        }
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



class ExceptionSample : FunSpec({
    fun boom(): Nothing = throw IllegalArgumentException("bad input")

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



class PasswordValidatorTest : FunSpec({
    class PasswordValidator {
        fun isValid(pw: String): Boolean =
            pw.length >= 8 &&
                    pw.any { it.isDigit() } &&
                    pw.any { it.isUpperCase() } &&
                    pw.any { it.isLowerCase() }
    }

    val v = PasswordValidator()

    test("여러 케이스를 데이터 기반으로 검증한다") {
        forAll(
            row("Abcdefg1", true),
            row("abcdefg1", false),   // 대문자 없음
            row("ABCDEFG1", false),   // 소문자 없음
            row("Abcdefgh", false),   // 숫자 없음
            row("A1b", false)         // 길이 부족
        ) { pw, expected ->
            v.isValid(pw) shouldBe expected
        }
    }
})



class CoroutineSample : FunSpec({
    test("suspend 함수 결과 검증") {
        suspend fun fetch(): Int { delay(10); return 42 }
        fetch() shouldBe 42
    }
})



private data class User(val id: Long, val name: String)

private interface UserRepository {
    suspend fun findById(id: Long): User?
}

private class UserService(private val repo: UserRepository) {
    suspend fun displayName(id: Long): String =
        repo.findById(id)?.name ?: throw NoSuchElementException("user $id")
}

class UserServiceTest : FunSpec({
    val repo = mockk<UserRepository>()
    val service = UserService(repo)

    test("존재하는 유저면 이름을 반환한다") {
        coEvery { repo.findById(1) } returns User(1, "Alice")

        service.displayName(1) shouldBe "Alice"
        coVerify(exactly = 1) { repo.findById(1) }
    }

    test("존재하지 않으면 NoSuchElementException") {
        coEvery { repo.findById(2) } returns null

        shouldThrow<NoSuchElementException> { service.displayName(2) }
        coVerify(exactly = 1) { repo.findById(2) }
    }
})



class CounterTest : FunSpec({
    class Counter {
        fun tick(n: Int, delayMs: Long): Flow<Int> = flow {
            repeat(n) { i ->
                delay(delayMs)
                emit(i + 1)
            }
        }
    }

    test("tick은 지정 개수만큼 순차 방출한다") {
        runTest {
            val values = Counter().tick(n = 3, delayMs = 10).toList()
            values shouldBe listOf(1, 2, 3)
        }
    }
})
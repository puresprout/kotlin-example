package com.purestation.app.mockk

import io.mockk.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

interface Calculator {
    fun add(a: Int, b: Int): Int
    fun log(message: String): Unit
}

class CalcUser(private val calc: Calculator) {
    fun addAndLog(a: Int, b: Int): Int {
        val r = calc.add(a, b)
        calc.log("sum=$r")
        return r
    }
}

class CalcUserTest {
    @Test
    fun `addAndLog calls add then log`() {
        val calc = mockk<Calculator>()

        // add 동작 정의
        every { calc.add(2, 3) } returns 5
        // Unit 함수는 빈 동작만 수행
        justRun { calc.log(any()) }

        val target = CalcUser(calc)
        val result = target.addAndLog(2, 3)

        assertEquals(5, result)

        // 정확히 1회 호출 검증
        verify(exactly = 1) { calc.add(2, 3) }
        verify { calc.log("sum=5") }

        // 그 외 호출이 없었는지 최종 확인
        confirmVerified(calc)
    }
}

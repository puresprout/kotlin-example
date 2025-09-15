package com.purestation.app.mockk

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// 확장함수는 top-level로?
fun File.readUtf8(): String = this.readText(Charsets.UTF_8)

class MockkTest {
    // ConfigServiceTest - 실제 파일 없이 읽기
    @Test
    fun test1() {
        class ConfigService {
            fun load(path: String): String = File(path).readUtf8()
        }

        mockkStatic(File::readUtf8)                   // 확장 함수 목킹
        every { File("/etc/app.conf").readUtf8() } returns "mode=dev"

        assertEquals("mode=dev", ConfigService().load("/etc/app.conf"))

        unmockkStatic(File::readUtf8)
    }

    // UseCaseTest
    @Test
    fun test2() {
        class Reporter {
            fun report(msg: String) { /* 네트워크 전송 */
            }
        }

        // 내부에서 Reporter()를 직접 만들기 때문에 DI가 어려운 상황을 가정
        class UseCase {
            fun send(msg: String) {
                if (msg.isNotBlank()) {
                    Reporter().report("[IMPORTANT] $msg")  // 내부에서 직접 생성
                }
            }
        }

        // 메시지가 있으면 Reporter 호출
        mockkConstructor(Reporter::class)
        every { anyConstructed<Reporter>().report(any()) } just Runs

        UseCase().send("hello")

        verify { anyConstructed<Reporter>().report("[IMPORTANT] hello") }
        unmockkConstructor(Reporter::class)


        // 빈 메시지면 Reporter 미호출
        mockkConstructor(Reporter::class)
        every { anyConstructed<Reporter>().report(any()) } just Runs

        UseCase().send("")

        verify(exactly = 0) { anyConstructed<Reporter>().report(any()) }
        unmockkConstructor(Reporter::class)
    }

    object Clock {
        fun now(): Long = System.currentTimeMillis()
    }

    // SessionTest - 세션 활성-만료 시나리오
    @Test
    fun test3() {
        class Session(private val ttlMs: Long) {
            private val createdAt = Clock.now()
            fun isActive(): Boolean = Clock.now() < createdAt + ttlMs
        }

        mockkObject(Clock)
        every { Clock.now() } returnsMany listOf(1_000L, 1_300L, 1_500L) // 생성, 1차검사, 2차검사

        val s = Session(ttlMs = 400) // 1000, 만료 시각 = 1400
        assertTrue(s.isActive())     // 1300 < 1400 → 활성
        assertFalse(s.isActive())    // 1500 >= 1400 → 만료

        unmockkObject(Clock)
    }

    // DiscountServiceTest - 비싼 내부 로직만 대체하고 최종 흐름 검증
    @Test
    fun test4() {
        class DiscountService {
            fun finalPrice(base: Int): Int = base - calcDiscount(base)
            fun calcDiscount(base: Int): Int {
                // 실제로는 복잡/외부요인 의존(예: 원격 규칙, DB)이라고 가정
                Thread.sleep(1000)
                return (base * 0.15).toInt()
            }
        }

        val svc = spyk(DiscountService())
        every { svc.calcDiscount(100) } returns 20   // 이 부분만 스텁

        assertEquals(80, svc.finalPrice(100))        // finalPrice는 실제 로직
        verify { svc.calcDiscount(100) }
    }
}

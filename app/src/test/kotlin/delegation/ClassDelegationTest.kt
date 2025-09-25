package com.purestation.app.delegation

import kotlin.test.Test

class ClassDelegationTest {
    interface Printer {
        fun print(msg: String)
    }

    class ConsolePrinter : Printer {
        override fun print(msg: String) = println(msg)
    }

    class PrinterProxy(printer: Printer) : Printer by printer

    // Printer 구현을 delegate에게 위임. 현 구현에서는 사실 by 필요없음
    class TimestampPrinter(private val delegate: Printer) : Printer by delegate {
        override fun print(msg: String) { // 필요한 것만 오버라이드해 부가 기능 추가
            val now = java.time.LocalDateTime.now()
            delegate.print("[$now] $msg")
        }
    }

    @Test
    fun test1() {
        var p: Printer = PrinterProxy(ConsolePrinter())
        p.print("Hello!")

        p = TimestampPrinter(ConsolePrinter())
        p.print("Hello!")  // 실제 출력은 ConsolePrinter가 수행, 앞에 타임스탬프만 추가
    }


    interface Logger { fun log(s: String) }
    interface Counter { var count: Int }

    class DefaultLogger : Logger { override fun log(s: String) = println(s) }
    class DefaultCounter : Counter { override var count: Int = 0 }

    class Service(
        private val logger: Logger = DefaultLogger(),
        private val counter: Counter = DefaultCounter()
    ) : Logger by logger, Counter by counter {

        fun doWork() {
            log("start")
            count++
            log("count = $count")
        }
    }

    @Test
    fun test2() {
        val service = Service()
        service.doWork()
    }
}
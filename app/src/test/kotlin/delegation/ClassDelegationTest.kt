package com.purestation.app.delegation

import kotlin.test.Test

class ClassDelegationTest {
    interface DataProcessor {
        fun process(data: String): String
        fun validate(data: String): Boolean
        fun save(data: String)
    }

    class DefaultProcessor : DataProcessor {
        override fun process(data: String): String {
            return data.trim()
        }

        override fun validate(data: String): Boolean {
            return data.isNotEmpty()
        }

        override fun save(data: String) {
            println("Saved: $data")
        }
    }

    // 부가기능: 처리된 데이터에 타임스탬프 붙이기
    class TimestampProcessor(private val delegate: DataProcessor) : DataProcessor by delegate {
        override fun process(data: String): String {
            val now = java.time.LocalDateTime.now()
            val processed = delegate.process(data)
            return "[$now] $processed"
        }
    }

    @Test
    fun test1() {
        val p1: DataProcessor = DefaultProcessor()
        val raw = "   hello world   "
        if (p1.validate(raw)) {
            val result = p1.process(raw)
            p1.save(result)
        }

        println("---")

        val p2: DataProcessor = TimestampProcessor(DefaultProcessor())
        if (p2.validate(raw)) { // validate는 delegate에 자동 위임
            val result = p2.process(raw) // process만 오버라이드
            p2.save(result) // save도 delegate에 자동 위임
        }
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
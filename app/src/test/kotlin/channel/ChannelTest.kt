package com.purestation.app.channel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ChannelTest {

    // Channel로 생산자-소비자(팬아웃) 구현
    @Test
    fun test1() = runTest {
        val ch = Channel<Int>(Channel.UNLIMITED)

        val producer = launch {
            for (i in 1..10) {
                delay(50)
                ch.send(i)
            }
            ch.close()
        }

        val workers = List(3) { w ->
            launch {
                for (x in ch) {
                    println("Worker$w handling $x")
                    delay(200)
                }
            }
        }

        producer.join()
        workers.joinAll()
    }

    // Fan-in (여러 프로듀서 → 하나의 채널)
    @Test
    fun test2() = runTest {
        val ch = Channel<String>()

        // producer1
        launch {
            repeat(5) {
                ch.send("🍎 from P1 [$it]")
                delay(200)
            }
        }

        // producer2
        launch {
            repeat(5) {
                ch.send("🍌 from P2 [$it]")
                delay(300)
            }
        }

        // consumer
        launch {
            repeat(10) {
                println("C <- ${ch.receive()}")
            }
            ch.close()
        }
    }
}
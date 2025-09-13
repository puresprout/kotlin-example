package com.purestation.app.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { delay(100) }
        } finally {
            // 코루틴이 취소되면 여기 들어옴
            withContext(NonCancellable) {
                println("리소스 정리 시작")
                delay(500) // 이미 취소 상태여도 정상 실행됨
                println("정리 완료")
            }
        }
    }

    // delay없이 cancel하면 코루틴이 실행하기도 전에 취소되서 println으로 안 찍힐수 있음
    delay(300)

    job.cancelAndJoin()
}
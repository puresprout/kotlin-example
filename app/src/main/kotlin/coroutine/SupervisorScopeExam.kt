package com.purestation.app.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

// O
//fun main() = runBlocking {
//    launch {
//        supervisorScope {
//            launch {
//                println("첫 번째 자식 시작")
//                throw RuntimeException("첫 번째 자식 에러!")
//            }
//            launch {
//                delay(1000)
//                println("두 번째 자식은 정상적으로 실행됨")
//            }
//        }
//    }.join()
//}

// X
//fun main() = runBlocking {
//    val scope = CoroutineScope(SupervisorJob())
//
//    // SupervisorJob은 직계 자식에게만 영향이 있어. 지금 구조에선 두 자식의 직계 부모가 SupervisorJob이 아니라 바깥 launch(Job) 라서 보호를 못 받는 상황
//    // 따라서 "두 번째 자식은 정상적으로 실행됨 출력 안됨"
//    scope.launch {
//        launch {
//            println("첫 번째 자식 시작")
//            throw RuntimeException("첫 번째 자식 에러!")
//        }
//        launch {
//            delay(1000)
//            println("두 번째 자식은 정상적으로 실행됨")
//        }
//    }.join() // 부모 launch가 끝날 때까지 대기
//}

fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())

    val c1 = scope.launch {
        println("첫 번째 자식 시작")
        throw RuntimeException("첫 번째 자식 에러!")
    }
    val c2 = scope.launch {
        delay(1000)
        println("두 번째 자식은 정상적으로 실행됨")
    }

    // 관찰용: 둘 다 끝날 때까지 대기
    joinAll(c1, c2)
}
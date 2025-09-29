package com.purestation.app.flow

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * 필터링 (Filtering)
 *
 * filter, filterNot, take, drop
 * distinctUntilChanged
 * sample (일정 주기마다 최근 값만 방출)
 */
class FlowFilteringTest {
    @Test
    fun test1() = runTest {
        val values = flow {
            emit(1); emit(1)
            emit(2); emit(2)
            emit(3); emit(3)
        }

        values
            .distinctUntilChanged()        // 연속 중복 제거
            .collect { value ->
                println("consume $value")  // 결과: 1, 2, 3
            }
    }


    @Test
    fun test2() = runTest {
        flowOf(1, 1, 2, 2, 2, 3, 1, 1)
            .distinctUntilChanged()
            .collect { println(it) } // 1, 2, 3, 1


        data class User(val id: Int, val name: String)

        flowOf(
            User(1, "Ann"),
            User(1, "Ann"),
            User(1, "Ann Lee"),
            User(2, "Bob")
        )
            .distinctUntilChanged { old, new -> old.id == new.id && old.name == new.name }
            .collect { println(it) }
        /*
        User(id=1, name=Ann)
        User(id=1, name=Ann Lee)
        User(id=2, name=Bob)
         */
    }
}
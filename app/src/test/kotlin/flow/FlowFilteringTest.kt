package com.purestation.app.flow

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
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
}
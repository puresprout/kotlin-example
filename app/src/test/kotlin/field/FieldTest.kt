package com.purestation.app.field

import kotlin.test.Test

class FieldTest {
    class User(name: String) {
        var name: String = ""
            set(value) {
                require(value.isNotBlank())
                field = value // field를 쓰면 backing field 생성
            }

        val upper: String
            get() = name.uppercase() // 계산 프로퍼티: backing field 없음

        init {
            this.name = name   // 여기서 setter 강제 호출
        }
    }

    @Test
    fun fieldTest() {
        User("psh").also { println(it.upper) }
//        User("").also { println(it.upper) }
    }
}
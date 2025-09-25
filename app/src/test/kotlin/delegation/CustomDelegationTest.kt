package com.purestation.app.delegation

import kotlin.reflect.KProperty
import kotlin.test.Test

class CustomDelegationTest {
    class MyDelegate<T>(initial: T) {
        private var stored: T = initial

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            println("$property 에서 값 '$stored' 를 가져옵니다.")

            return stored
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            println("$property 에 값 '$value' 를 설정합니다.")

            stored = value
        }
    }

    class Example {
        var text: String by MyDelegate("")
    }

    @Test
    fun test1() {
        val ex = Example()
        ex.text = "Hello"
        println(ex.text)
    }
}
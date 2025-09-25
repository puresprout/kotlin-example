package com.purestation.app.delegation

import kotlin.properties.Delegates
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

    @Test
    fun test2() {
        fun loadConfigFromDisk(): String {
            println("loadConfigFromDisk")

            return "config"
        }

        // 최초 접근 시 1회만 계산·저장, 이후 캐시 사용
        val config: String by lazy {
            loadConfigFromDisk()
        }

        println("config = $config")       // ← 첫 접근: loadConfigFromDisk() 실행
        println("config again = $config") // ← 두 번째부터는 캐시 사용(호출 안 됨)
    }

    @Test
    fun test3() {
        var name: String by Delegates.observable("<init>") { prop, old, new ->
            println("${prop.name} : $old -> $new")
        }

        println("초기값 = $name")   // "<init>"
        name = "Alice"             // 콜백 호출
        name = "Bob"               // 콜백 호출
        name = "Bob"               // 값이 같아도 콜백은 호출됨
    }

    @Test
    fun test4() {
        class RangeDelegate(private val range: IntRange) {
            private var field: Int = range.first
            operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = field
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                require(value in range) { "${property.name} must be in $range: $value" }
                field = value
            }
        }

        var percentage: Int by RangeDelegate(0..100)

        println(percentage)
        percentage = 80      // OK
        println(percentage)
        // percentage = 120  // 예외 발생
    }

    @Test
    fun test5() {
        class LogDelegate<T>(private var field: T) {
            operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
                println("get ${p.name} -> $field")
                return field
            }
            operator fun setValue(thisRef: Any?, p: KProperty<*>, value: T) {
                println("set ${p.name} : $field -> $value")
                field = value
            }
        }

        var title: String by LogDelegate("init")

        println(title)
    }
}
package com.purestation.app.reflection

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test

class ReflectionTest {
    @Test
    fun test() {
        data class Person(val name: String, private val age: Int)

        fun printProps(any: Any) {
            val k = any::class
            for (p in k.memberProperties) {
                // java.lang.IllegalAccessException: class kotlin.reflect.jvm.internal.calls.CallerImpl$FieldGetter cannot access a member of class com.purestation.app.reflection.ReflectionTest$test$Person with modifiers "private final"
                p.isAccessible = true
                println("${p.name} = ${p.getter.call(any)}")
            }
        }

        printProps(Person("Alice", 23))
    }
}
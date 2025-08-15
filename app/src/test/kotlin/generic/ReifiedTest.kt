package com.purestation.app.generic

import kotlin.test.Test

class ReifiedTest {
    class MyClassA
    class MyClassB

    @Test
    fun testReified() {
        val myClassA = MyClassA()

        if (myClassA.isA<MyClassA>()) {
            println("MyClassA")
        } else {
            println("Not MyClassA")
        }
    }

    inline fun <reified T> Any?.isA(): Boolean = this is T

    inline fun <reified T> Any?.asOrNull(): T? = this as? T
}
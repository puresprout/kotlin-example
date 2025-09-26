package com.purestation.app.operator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class OperatorTest {
    @Test
    fun test1() {
        operator fun Int.times(str: String): String = str.repeat(this)

        println(3 * "Hi ") // "Hi Hi Hi "
    }

    @Test
    fun test2() {
        class Bag<T>(private val list: MutableList<T> = mutableListOf()) : Iterable<T> {
            operator fun plusAssign(item: T) { list.add(item) }   // 복합 대입
            operator fun contains(item: T) = item in list         // in
            override fun iterator(): Iterator<T> = list.iterator()// for-in

            override fun toString() = list.toString()
        }

        val bag = Bag<Int>()
        bag += 1
        bag += 2
        println(2 in bag) // true
        for (e in bag) print("$e ") // 1 2
    }

    @Test
    fun test3() {
        data class Point(val x: Int, val y: Int)
        data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
            operator fun contains(p: Point): Boolean =
                p.x in left..right && p.y in top..bottom
        }

        val r = Rect(0, 0, 10, 10)
        assertTrue(Point(3, 4) in r)   // true
        assertFalse(Point(20, 5) in r)  // false
    }

    @Test
    fun test4() {
        data class Vec2(val x: Double, val y: Double) : Comparable<Vec2> {
            // 산술
            operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
            operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
            operator fun times(k: Double) = Vec2(x * k, y * k)
            operator fun div(k: Double) = Vec2(x / k, y / k)
            operator fun rem(k: Double) = Vec2(x % k, y % k)

            // 단항
            operator fun unaryMinus() = Vec2(-x, -y)
            operator fun unaryPlus() = this

            // 비교(길이 기준)
            override operator fun compareTo(other: Vec2): Int =
                (x * x + y * y).compareTo(other.x * other.x + other.y * other.y)

            // 인덱스 접근
            operator fun get(i: Int): Double = when (i) {
                0 -> x
                1 -> y
                else -> throw IndexOutOfBoundsException("$i")
            }

            // 구조 분해(component1/2)는 data class가 자동 생성
        }

        val a = Vec2(3.0, 4.0)
        val b = Vec2(1.0, 2.0)

        println(a + b)          // Vec2(x=4.0, y=6.0)
        println(a * 2.0)        // Vec2(x=6.0, y=8.0)
        println(-a)             // Vec2(x=-3.0, y=-4.0)

        val (x, y) = a          // component1/2
        println("$x, $y")       // 3.0, 4.0

        println(a[0])           // 3.0
        println(a > b)          // true (길이 비교)
    }
}
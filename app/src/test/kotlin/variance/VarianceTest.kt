package com.purestation.app.variance

import kotlin.test.Test

class VarianceTest {
    open class Animal
    class Cat : Animal()
    class Dog : Animal()

    @Test
    fun invariantTest() {
        // MutableList<E>
        fun printAnimals(animals: MutableList<Animal>) {
            animals.forEach(::println)
        }

        val animals = mutableListOf(Animal())
        val cats = mutableListOf(Cat())

        printAnimals(animals)
//        printAnimals(cats)  // 컴파일 에러 (불공변성)
 }

    @Test
    fun covariantTest() {
        // List<out E>
        fun printAnimals(animals: List<Animal>) {
//            animals.forEach { println(it) }

            for (i in 0 until animals.size) {
                println(animals[i])
            }
        }

        val animals = listOf(Animal())
        val cats = listOf(Cat())

        printAnimals(animals)  // OK (공변성 덕분)
        printAnimals(cats)  // OK (공변성 덕분)
    }

    @Test
    fun contravariantTest1() {
        // Cat만 받는 핸들러가 필요
        fun callCat(handler: (Cat) -> Unit) {
            handler(Cat())
        }

        // 더 넓은 타입(Animal)을 받아도 Cat 처리 가능
        val animalHandler: (Animal) -> Unit = { println("animal handle: ${it::class.simpleName}") }
        val catHandler: (Cat) -> Unit = { println("cat handle: ${it::class.simpleName}") }

        callCat(animalHandler)  // OK: 파라미터가 반공변이라 대입 가능
        callCat(catHandler)
    }

    fun interface Sink<in T> {
        fun accept(value: T)
    }

    @Test
    fun contravariantTest2() {
        val animalSink: Sink<Animal> = Sink { println("animal sink: $it") }
        val catSink: Sink<Cat> = Sink { println("cat sink: $it") }

        // Cat 전용 Sink가 필요한 자리에 Animal Sink를 넣을 수 있음
        var sink: Sink<Cat> = animalSink
        sink.accept(Cat())   // 출력: sink: Cat

        sink = catSink
        sink.accept(Cat())   // 출력: sink: Cat
    }

    @Test
    fun contravariantTest3() {
        fun moveCats(src: List<Cat>, dst: MutableList<in Cat>) {
            for (c in src) dst.add(c)
        }

        val animals: MutableList<Animal> = mutableListOf()
        println(animals)

        val cats: List<Cat> = listOf(Cat(), Cat())

        moveCats(cats, animals)   // OK: MutableList<Animal> 은 MutableList<in Cat> 로 사용 가능
        println(animals)

        val dstCats: MutableList<Cat> = mutableListOf()
        println(dstCats)

        moveCats(cats, dstCats)
        println(dstCats)
    }
}
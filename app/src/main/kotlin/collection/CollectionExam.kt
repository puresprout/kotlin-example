package com.purestation.app.collection

fun main() {
    val numbers = listOf(1,2,3,4,5)

    println(numbers.sum())
    println(numbers.average())



    data class Subject(val name: String, val score: Int)

    val subjects = listOf(Subject("Math", 90), Subject("Science", 80))

    println(subjects.sumOf { it.score })
    println(subjects.map { it.score }.average())
}
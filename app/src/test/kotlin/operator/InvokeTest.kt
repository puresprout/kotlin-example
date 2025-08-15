package com.purestation.app.operator

import kotlin.test.Test

class InvokeTest {
    @Test
    fun test() {
//        GreeterUseCase().invoke("psh")

        val greeterUseCase = GreeterUseCase()
        greeterUseCase("psh")
    }

    class GreeterUseCase {
        operator fun invoke(name: String) {
            println("Hello $name")
        }
    }
}
package com.purestation.app.scope

import kotlin.test.Test

class ScopeFunctionTest {

    @Test
    fun test1() {
        val str: String? = "Hello"

        // let: null-safe, 결과 반환 가능
        val len = str?.let {
            println("let: 문자열 길이 = ${it.length}")
            it.length
        }

        // run: 객체 내부 로직 실행 후 마지막 줄 반환
        val result = str?.run {
            println("run: 대문자 = ${this.uppercase()}")
            length
        }

        // with: null 허용 X, 일반 객체 스코프에서 실행
        val msg = with(str!!) {
            println("with: 소문자 = ${lowercase()}")
            "길이는 $length 입니다"
        }

        // apply: 객체 자기 자신 반환, 주로 초기화에 사용
        val sb = StringBuilder().apply {
            append("apply: ")
            append(str)
        }
        println(sb.toString())

        // also: 부가 작업 (logging 등)에 사용, 자기 자신 반환
        val newStr = str?.also {
            println("also: 원래 값 = $it")
        }

        println("len=$len, result=$result, msg=$msg, newStr=$newStr")
    }
}

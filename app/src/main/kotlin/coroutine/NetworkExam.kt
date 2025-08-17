package com.purestation.app.coroutine

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MyClient {

    // 데이터를 가져오는 suspend 함수
    suspend fun fetchData(): String {
        return fetchFromNetwork("https://jsonplaceholder.typicode.com/todos/1")
    }

    // 실제 네트워크 요청 (IO 디스패처에서 실행)
    private suspend fun fetchFromNetwork(urlString: String): String =
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    throw Exception("HTTP error code: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        }
}

fun main() = runBlocking {
    val client = MyClient()

    try {
        val result = client.fetchData()
        println("네트워크 응답: $result")
    } catch (e: Exception) {
        println("네트워크 오류: ${e.message}")
    }
}

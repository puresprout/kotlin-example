package com.purestation.app.mockk

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class MockkAnnotationStyleTest {
    data class User(val id: Long, val name: String)

    interface UserRepository {
        fun findUser(id: Long): User?
        fun save(user: User)
    }

    interface Notifier {
        fun send(userId: Long, msg: String)
    }

    class UserService(
        private val repo: UserRepository,
        private val notifier: Notifier,
        private val cache: MutableMap<Long, User>
    ) {
        fun rename(id: Long, newName: String): User {
            val u = cache[id] ?: repo.findUser(id) ?: error("not found")
            val updated = u.copy(name = newName)
            repo.save(updated)
            cache[id] = updated
            notifier.send(id, "renamed to $newName")
            return updated
        }
    }

    @MockK
    lateinit var repo: UserRepository

    @RelaxedMockK
    lateinit var notifier: Notifier

    var cache = mutableMapOf<Long, User>()

    @InjectMockKs
    lateinit var sut: UserService  // repo/notifier/cache 자동 주입

    @Test
    fun `애노테이션 목들 사용`() {
        every { repo.findUser(1L) } returns User(1L, "Alice")
        every { repo.save(any()) } just Runs

        val result = sut.rename(1L, "Bob")

        assertEquals("Bob", result.name)
        assertEquals("Bob", cache[1L]?.name)
        verify {
            repo.findUser(1L)
            repo.save(User(1L, "Bob"))
            notifier.send(1L, match { it.contains("Bob") })
        }
        confirmVerified(repo, notifier)
    }
}

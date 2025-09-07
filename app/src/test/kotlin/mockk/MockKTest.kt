package com.purestation.app.mockk

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface Calculator {
    fun add(a: Int, b: Int): Int
    fun log(message: String): Unit
}

class CalcUser(private val calc: Calculator) {
    fun addAndLog(a: Int, b: Int): Int {
        val r = calc.add(a, b)
        calc.log("sum=$r")
        return r
    }
}

class CalcUserTest {
    @Test
    fun `addAndLog calls add then log`() {
        val calc = mockk<Calculator>()

        // add 동작 정의
        every { calc.add(2, 3) } returns 5
        // Unit 함수는 빈 동작만 수행
        justRun { calc.log(any()) }

        val target = CalcUser(calc)
        val result = target.addAndLog(2, 3)

        assertEquals(5, result)

        // 정확히 1회 호출 검증
        verify(exactly = 1) { calc.add(2, 3) }
        verify { calc.log("sum=5") }

        // 그 외 호출이 없었는지 최종 확인
        confirmVerified(calc)
    }
}



interface UserApi { suspend fun fetchUser(id: Long): User }
data class User(val id: Long, val name: String)

class UserService(private val api: UserApi) {
    suspend fun nameOf(id: Long): String = api.fetchUser(id).name
}

class UserServiceTest {
    @Test
    fun `suspend stub with coEvery`() = runBlocking {
        val api = mockk<UserApi>()

        // suspend 함수 스텁
        coEvery { api.fetchUser(42) } returns User(42, "Alice")

        val svc = UserService(api)
        assertEquals("Alice", svc.nameOf(42))

        // suspend 함수 호출 검증
        coVerify(exactly = 1) { api.fetchUser(42) }
    }
}



interface Repo { fun save(payload: String): Boolean }

class UseCase(private val repo: Repo) {
    fun run(input: String): Boolean = repo.save("prefix:$input")
}

class UseCaseTest {
    @Test
    fun `capture argument`() {
        val repo = mockk<Repo>()
        val arg = slot<String>()

        every { repo.save(capture(arg)) } answers {
            // 캡처된 인자 검사 후 결과 결정
            arg.captured.startsWith("prefix:")
        }

        val uc = UseCase(repo)
        val ok = uc.run("data")

        assertEquals(true, ok)
        assertEquals("prefix:data", arg.captured)
        verify { repo.save(any()) }
    }
}



interface A { fun step1() }
interface B { fun step2() }

class OrderTest {
    @Test
    fun `verify order & sequence`() {
        val a = mockk<A>()
        val b = mockk<B>()
        every { a.step1() } returns Unit
        every { b.step2() } returns Unit
//        justRun { a.step1() }
//        justRun { b.step2() }

        a.step1()
        b.step2()
        a.step1()

        // 순서만 검증
        verifyOrder {
            a.step1()
            b.step2()
            a.step1()
        }

        // 정확히 이 시퀀스만 허용
        verifySequence {
            a.step1()
            b.step2()
            a.step1()
        }
    }
}



open class PriceCalc {
    open fun base() = 100
    open fun tax() = 10
    fun total() = base() + tax()
}

class SpyTest {
    @Test
    fun `spyk keeps real logic unless stubbed`() {
        val spy = spyk(PriceCalc())
        // 세금만 목킹
        every { spy.tax() } returns 5
        // base()는 실제 호출 → 100
        assertEquals(105, spy.total())
        verify { spy.tax() }
    }
}



interface Parser { fun parse(s: String): Int }

class ParserTest {
    @Test
    fun `returnsMany, throws, returnsArgument`() {
        val p = mockk<Parser>()

        every { p.parse(any()) } answers { firstArg<String>().length }
        every { p.parse("seq") } returnsMany listOf(1, 2, 3)
        every { p.parse("boom") } throws IllegalArgumentException("bad")

        assertEquals(1, p.parse("seq"))
        assertEquals(2, p.parse("seq"))
        assertEquals(3, p.parse("seq"))
        assertEquals(4, p.parse("wow!"))
        assertFailsWith<IllegalArgumentException> { p.parse("boom") }
    }
}



object TokenProvider { fun issue() = "real-token" }
class Util { fun nowEpoch() = System.currentTimeMillis() }
class Client {
    fun newUtil(): Util = Util()
    fun login(): String = TokenProvider.issue()
    fun timestamp(): Long = newUtil().nowEpoch()
}

class HardToMockTest {
    @Test
    fun `mock object, static, constructor`() {
        val client = Client()

        // object 목킹
        mockkObject(TokenProvider)
        every { TokenProvider.issue() } returns "fake-token"

        // 생성자 목킹(생성된 모든 Util 인스턴스 가로채기)
        mockkConstructor(Util::class)
        every { anyConstructed<Util>().nowEpoch() } returns 1234L

        assertEquals("fake-token", client.login())
        assertEquals(1234L, client.timestamp())

        // 해제
        unmockkObject(TokenProvider)
        unmockkAll()
    }
}



// 도메인과 계약
data class Post(val id: Long, val title: String)
interface PostRepository {
    suspend fun find(id: Long): Post?
    suspend fun save(post: Post): Unit
}

class PostService(private val repo: PostRepository) {
    suspend fun uppercaseTitle(id: Long): Boolean {
        val p = repo.find(id) ?: return false
        repo.save(p.copy(title = p.title.uppercase()))
        return true
    }
}

class PostServiceTest {
    @Test
    fun `uppercaseTitle updates when found`() = runBlocking {
        val repo = mockk<PostRepository>()
        coEvery { repo.find(10) } returns Post(10, "hello")
        coJustRun { repo.save(any()) } // suspend Unit 함수 빈 동작

        val svc = PostService(repo)
        val ok = svc.uppercaseTitle(10)

        assertTrue(ok)
        coVerify { repo.find(10) }
        coVerify { repo.save(match { it.title == "HELLO" }) }
        confirmVerified(repo)
    }

    @Test
    fun `uppercaseTitle returns false when missing`() = runBlocking {
        val repo = mockk<PostRepository>()
        coEvery { repo.find(99) } returns null

        val svc = PostService(repo)
        assertFalse(svc.uppercaseTitle(99))

        coVerify(exactly = 1) { repo.find(99) }
        coVerify(exactly = 0) { repo.save(any()) }
    }
}

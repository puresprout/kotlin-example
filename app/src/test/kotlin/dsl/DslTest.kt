package com.purestation.app.dsl

import kotlin.test.Test

@DslMarker
annotation class HtmlDsl

interface Element {
    fun render(): String
}

@HtmlDsl
class Text(private val raw: String) : Element {
    override fun render() = raw
}

@HtmlDsl
class Ul : Element {
    private val children = mutableListOf<Element>()

    fun li(text: String) {
        children += Text("<li>$text</li>")
    }

    override fun render() = "<ul>\n${children.joinToString("") { it.render() + "\n" }}</ul>"
}

@HtmlDsl
class Body : Element {
    private val children = mutableListOf<Element>()

    fun h1(text: String) {
        children += Text("<h1>$text</h1>\n")
    }

    fun p(text: String) {
        children += Text("<p>$text</p>\n")
    }

    fun ul(block: Ul.() -> Unit) {
        children += Ul().apply(block)
    }

    override fun render() = "<body>\n${children.joinToString("") { it.render() }}\n</body>"
}

@HtmlDsl
class Html {
    private lateinit var body: Body

    fun body(block: Body.() -> Unit) {
        body = Body().apply(block)
    }

    fun render() = "<html>\n${body.render()}\n</html>"
}

fun html(block: Html.() -> Unit) = Html().apply(block).render()

class DslTest {
    @Test
    fun testHtmlDsl() {
        val page = html {
            body {
                h1("HTML DSL")
                p("There are many languages.")
                ul {
                    li("kotlin")
                    li("java")
                    li("python")

//                    body {
//                        p("invalid body")
//                    }
                }
            }
        }

        println(page)
    }
}
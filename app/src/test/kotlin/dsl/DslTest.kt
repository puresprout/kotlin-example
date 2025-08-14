package com.purestation.app.dsl

import kotlin.test.Test

class DslTest {
    @DslMarker
    annotation class HtmlDsl

    @HtmlDsl
    class Ul {
        fun li(text: String) { println("<li>$text</li>") }
    }

    @HtmlDsl
    class Body {
        fun p(text: String) { println("<p>$text</p>") }
        fun ul(block: Ul.() -> Unit) {
            Ul().apply(block)
        }
    }

    @HtmlDsl
    class Html {
        fun body(block: Body.() -> Unit) {
            Body().apply(block)
        }
    }

    fun html(block: Html.() -> Unit) {
        Html().apply(block)
    }

    @Test
    fun testHtmlDsl() {
        html {
            body {
                p("HTML DSL")
                ul {
                    li("html")
                    li("body")
                    li("p")
                    li("ul")
                    li("li")
                }
            }
        }
    }
}
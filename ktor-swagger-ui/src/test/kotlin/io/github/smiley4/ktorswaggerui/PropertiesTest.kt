package io.github.smiley4.ktorswaggerui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class PropertiesTest : StringSpec({

    "empty" {
        val result = PropertyBuilder().render(
            linePrefix = "      ",
            prefixFirst = false,
            separator = ",\n",
            nullBehavior = "include"
        )
        result shouldBeEqual """
        """.trimIndent()
    }

    "different types" {
        val result = PropertyBuilder().also {
            it["null"] = null
            it["int"] = 43
            it["double"] = 1.23
            it["string"] = "hello"
            it["boolean"] = true
            it["raw"] = PropertyBuilder.Raw("[{key: \"mykey\", value: \"myvalue\"}]")
        }.render(
            linePrefix = "  ",
            prefixFirst = false,
            separator = ",\n",
            nullBehavior = "include"
        )
        result shouldBeEqual """
        null: null,
          int: 43,
          double: 1.23,
          string: 'hello',
          boolean: true,
          raw: [{key: "mykey", value: "myvalue"}]
        """.trimIndent()
    }

})
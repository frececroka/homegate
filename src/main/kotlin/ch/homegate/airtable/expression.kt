package ch.homegate.airtable

sealed class Expression {
    data class Eq(val left: Expression, val right: Expression): Expression() {
        override fun toString(): String {
            return "($left) = ($right)"
        }
    }
    data class Search(val needle: Expression, val haystack: Expression): Expression() {
        override fun toString(): String {
            return "SEARCH($needle, $haystack)"
        }
    }
    data class S(val c: String): Expression() {
        override fun toString(): String {
            return "\"$c\""
        }
    }
    data class I(val c: Int): Expression() {
        override fun toString(): String {
            return c.toString()
        }
    }
    data class V(val n: String): Expression() {
        override fun toString(): String {
            return n
        }
    }
}

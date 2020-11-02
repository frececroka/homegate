package ch.homegate

fun <T> invertToString(vararg ts: T): (String) -> T =
    { s -> ts.first { it.toString() == s } }

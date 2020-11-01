package ch.homegate.crawler

import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup

sealed class ReplyOption {

    object Ignore : ReplyOption()
    object Contacted : ReplyOption()
    object Applied : ReplyOption()

    companion object {
        fun fromString(s: String) = when (s) {
            Ignore.toString() -> Ignore
            Contacted.toString() -> Contacted
            Applied.toString() -> Applied
            else -> throw IllegalArgumentException()
        }
    }

    override fun toString(): String = when (this) {
        Ignore -> "ignore"
        Contacted -> "contacted"
        Applied -> "applied"
    }

}

fun buildReplyKeyboard(selected: ReplyOption? = null): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(listOf(
        buildReplyKeyboardButton("Ignore", "ignore", selected == ReplyOption.Ignore),
        buildReplyKeyboardButton("Contacted", "contacted", selected == ReplyOption.Contacted),
        buildReplyKeyboardButton("Applied", "applied", selected == ReplyOption.Applied))))
}

private fun buildReplyKeyboardButton(title: String, data: String, selected: Boolean = false) =
    InlineKeyboardButton(title + if (selected) " ✅" else "", callbackData = data)

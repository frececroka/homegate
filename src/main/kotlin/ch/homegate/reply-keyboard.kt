package ch.homegate

import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup

sealed class ReplyOption {

    object Ignore : ReplyOption()
    object Contacted : ReplyOption()
    object Viewing : ReplyOption()
    object Applied : ReplyOption()

    companion object {
        fun fromString(s: String) =
            invertToString(Ignore, Contacted, Viewing, Applied)(s)
    }

    override fun toString(): String = when (this) {
        Ignore -> "ignore"
        Contacted -> "contacted"
        Viewing -> "viewing"
        Applied -> "applied"
    }

}

fun buildReplyKeyboard(selected: ReplyOption? = null): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(
        listOf(
            buildReplyKeyboardButton("Ignore", ReplyOption.Ignore, selected),
            buildReplyKeyboardButton("Contacted", ReplyOption.Contacted, selected)),
        listOf(
            buildReplyKeyboardButton("Viewing", ReplyOption.Viewing, selected),
            buildReplyKeyboardButton("Applied", ReplyOption.Applied, selected))))
}

private fun buildReplyKeyboardButton(title: String, option: ReplyOption, selected: ReplyOption? = null) =
    InlineKeyboardButton((if (selected == option) "✅ " else "") + title, callbackData = option.toString())

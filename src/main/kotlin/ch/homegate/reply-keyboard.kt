package ch.homegate

import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup

sealed class ReplyOption {

    object Delete : ReplyOption()
    object Ignore : ReplyOption()
    object Contacted : ReplyOption()
    object Viewing : ReplyOption()
    object Applied : ReplyOption()

    override fun toString(): String = when (this) {
        Delete -> "delete"
        Ignore -> "ignore"
        Contacted -> "contacted"
        Viewing -> "viewing"
        Applied -> "applied"
    }

}

fun buildReplyKeyboard(selected: ReplyOption? = null): InlineKeyboardMarkup {
    val dismissButton = if (selected == ReplyOption.Ignore) {
        // If the "Ignore" option was selected, offer to delete the message now.
        buildReplyKeyboardButton("Delete", ReplyOption.Delete)
    } else {
        // If any other option was selected, offer to ignore the message instead.
        buildReplyKeyboardButton("Ignore", ReplyOption.Ignore)
    }
    return InlineKeyboardMarkup(listOf(
        listOf(
            dismissButton,
            buildReplyKeyboardButton("Contacted", ReplyOption.Contacted, selected)),
        listOf(
            buildReplyKeyboardButton("Viewing", ReplyOption.Viewing, selected),
            buildReplyKeyboardButton("Applied", ReplyOption.Applied, selected))))
}

private fun buildReplyKeyboardButton(title: String, option: ReplyOption, selected: ReplyOption? = null) =
    InlineKeyboardButton((if (selected == option) "✅ " else "") + title, callbackData = option.toString())

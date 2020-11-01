package ch.homegate

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot

fun createBot(configure: Bot.Builder.() -> Unit): Bot = bot {
    token = System.getenv("TELEGRAM_TOKEN")
    configure(this)
}

package ch.homegate

import java.util.logging.Level
import java.util.logging.Logger

fun setupJavaLogging() {
    Logger.getLogger("okhttp3").level = Level.WARNING
}

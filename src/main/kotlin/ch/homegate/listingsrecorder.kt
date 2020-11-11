package ch.homegate

import com.google.cloud.firestore.CollectionReference
import com.google.common.reflect.TypeToken

/**
 * Implementations keep track of the listings for which Telegram messages have already been sent.
 */
interface ListingsRecorder {
    fun add(homegateId: String, chatId: Long, messageId: Long)
    fun getTelegramId(id: String): Pair<Long, Long>?
    fun getHomegateId(telegramId: Pair<Long, Long>): String?
}

@Suppress("unused")
class LocalListingsRecorder(
    private val db: JsonDb
) : ListingsRecorder {

    override fun add(homegateId: String, chatId: Long, messageId: Long) =
        db.set(homegateId, Pair(chatId, messageId))

    @Suppress("UnstableApiUsage")
    private val homegateIdType = (object : TypeToken<Pair<Long, Long>>() {}).type

    override fun getTelegramId(id: String): Pair<Long, Long>? =
        db.get<Pair<Long, Long>>(id, homegateIdType)

    override fun getHomegateId(telegramId: Pair<Long, Long>): String? {
        val record = db.find<Pair<Long, Long>>({ it == telegramId }, homegateIdType)
        return if (record != null) {
            val (id, _) = record; id
        } else null
    }

}

@Suppress("unused")
class FirestoreListingsRecorder(private val collection: CollectionReference) : ListingsRecorder {

    data class Entry(
        val chatId: Long = 0,
        val messageId: Long = 0,
        val created: Long = System.currentTimeMillis()
    )

    override fun add(homegateId: String, chatId: Long, messageId: Long) {
        val entry = Entry(chatId, messageId)
        collection.document(homegateId).create(entry).get()
    }

    override fun getTelegramId(id: String): Pair<Long, Long>? {
        val document = collection.document(id).get().get().toObject(Entry::class.java)
            ?: return null
        return Pair(document.chatId, document.messageId)
    }

    override fun getHomegateId(telegramId: Pair<Long, Long>): String? {
        val (chatId, messageId) = telegramId
        val result = collection
            .whereEqualTo("chatId", chatId)
            .whereEqualTo("messageId", messageId)
            .get().get()
        val document = result.documents.singleOrNull()
        return document?.id
    }

}

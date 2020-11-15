package ch.homegate

import com.google.cloud.firestore.CollectionReference
import com.google.common.reflect.TypeToken
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

/**
 * Implementations keep track of the listings for which Telegram messages have already been sent.
 */
interface ListingsRecorder {
    fun add(homegateId: String, chatId: Long, messageId: Long)
    fun getMessageId(homegateId: String, chatId: Long): Long?
    fun getHomegateId(telegramId: Pair<Long, Long>): String?
}

@Component
@Profile("local")
@Suppress("unused")
class LocalListingsRecorder(
    @Qualifier("listings-db") private val db: JsonDb
) : ListingsRecorder {

    @Suppress("UnstableApiUsage")
    private val type = (object : TypeToken<Triple<String, Long, Long>>() {}).type

    override fun add(homegateId: String, chatId: Long, messageId: Long) =
        db.set(UUID.randomUUID().toString(), Triple(homegateId, chatId, messageId))

    override fun getMessageId(homegateId: String, chatId: Long): Long? {
        val (_, value) = db.find<Triple<String, Long, Long>>({ (h, c, _) ->
            h == homegateId && c == chatId
        }, type) ?: return null
        val (_, _, messageId) = value
        return messageId
    }

    override fun getHomegateId(telegramId: Pair<Long, Long>): String? {
        val record = db.find<Triple<String, Long, Long>>({ (_, c, m) ->
            telegramId == Pair(c, m)
        }, type) ?: return null
        val (_, value) = record
        val (homegateId, _, _) = value
        return homegateId
    }

}

@Component
@Profile("gcf")
@Suppress("unused")
class FirestoreListingsRecorder(
    @Qualifier("listings-db") private val collection: CollectionReference
) : ListingsRecorder {

    data class Entry(
        val homegateId: String = "",
        val chatId: Long = 0,
        val messageId: Long = 0,
        val created: Long = System.currentTimeMillis()
    )

    override fun add(homegateId: String, chatId: Long, messageId: Long) {
        val entry = Entry(homegateId, chatId, messageId)
        collection.document().create(entry).get()
    }

    override fun getMessageId(homegateId: String, chatId: Long): Long? {
        val result = collection
            .whereEqualTo("homegateId", homegateId)
            .whereEqualTo("chatId", chatId)
            .get().get()
        val document = result.documents.singleOrNull() ?: return null
        val entry = document.toObject(Entry::class.java)
        return entry.messageId
    }

    override fun getHomegateId(telegramId: Pair<Long, Long>): String? {
        val (chatId, messageId) = telegramId
        val result = collection
            .whereEqualTo("chatId", chatId)
            .whereEqualTo("messageId", messageId)
            .get().get()
        val document = result.documents.singleOrNull() ?: return null
        val entry = document.toObject(Entry::class.java)
        return entry.homegateId
    }

}

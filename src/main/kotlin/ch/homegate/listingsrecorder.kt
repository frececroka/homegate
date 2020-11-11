package ch.homegate

import com.google.cloud.firestore.CollectionReference

/**
 * Implementations keep track of the listings for which Telegram messages have already been sent.
 */
interface ListingsRecorder {
    fun add(id: String, messageId: Long)
    fun getMessageId(id: String): Long?
    fun getId(messageId: Long): String?
}

@Suppress("unused")
class LocalListingsRecorder(
    private val db: JsonDb
) : ListingsRecorder {

    override fun add(id: String, messageId: Long) =
        db.set(id, messageId)

    override fun getMessageId(id: String): Long? =
        db.get<Long>(id, Long::class.java)

    override fun getId(messageId: Long): String? {
        val record = db.find<Long>({ it == messageId }, Long::class.java)
        return if (record != null) {
            val (id, _) = record; id
        } else null
    }

}

@Suppress("unused")
class FirestoreListingsRecorder(private val collection: CollectionReference) : ListingsRecorder {

    data class Entry(
        val messageId: Long = 0,
        val created: Long = System.currentTimeMillis()
    )

    override fun add(id: String, messageId: Long) {
        val entry = Entry(messageId)
        collection.document(id).create(entry).get()
    }

    override fun getMessageId(id: String): Long? {
        val document = collection.document(id).get().get().toObject(Entry::class.java)
        return document?.messageId
    }

    override fun getId(messageId: Long): String? {
        val result = collection.whereEqualTo("messageId", messageId).get().get()
        val document = result.documents.singleOrNull()
        return document?.id
    }

}

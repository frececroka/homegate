package ch.homegate

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.FirestoreOptions
import java.io.FileNotFoundException
import java.nio.file.Paths

/**
 * Implementations keep track of the listings for which Telegram messages have already been sent.
 */
interface ListingsRecorder {
    fun add(id: String, messageId: Long)
    fun getMessageId(id: String): Long?
    fun getId(messageId: Long): String?
}

@Suppress("unused")
class LocalListingsRecorder : ListingsRecorder {

    private val root = Paths.get("listings")

    init {
        root.toFile().mkdir()
    }

    override fun add(id: String, messageId: Long) {
        representative(id).writeText(messageId.toString())
    }

    override fun getMessageId(id: String): Long? {
        return try {
            representative(id).readText().toLong()
        } catch (_: FileNotFoundException) {
            null
        }
    }

    override fun getId(messageId: Long): String? {
        val files = root.toFile().listFiles()!!
        val file = files.firstOrNull { getMessageId(it.name) == messageId }
        return file?.name
    }

    private fun representative(id: String) = root.resolve(id).toFile()

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

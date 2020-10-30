package ch.homegate.crawler

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import java.nio.file.Paths

interface ListingsRecorder {
    fun add(id: String): Boolean
    fun isNew(id: String): Boolean
}

@Suppress("unused")
class LocalListingsRecorder : ListingsRecorder {

    private val root = Paths.get("listings")

    init {
        root.toFile().mkdir()
    }

    override fun add(id: String) = representative(id).createNewFile()
    override fun isNew(id: String) = !representative(id).exists()
    private fun representative(id: String) = root.resolve(id).toFile()
}

@Suppress("unused")
class FirestoreListingsRecorder : ListingsRecorder {

    data class Entry(val created: Long = System.currentTimeMillis())

    private val firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build()
    private val db = firestoreOptions.service
    private val collection = db.collection("listings")

    override fun add(id: String): Boolean {
        collection.document(id).create(Entry()).get()
        return true
    }

    override fun isNew(id: String): Boolean {
        val document = collection.document(id).get().get()
        return !document.exists()
    }

}

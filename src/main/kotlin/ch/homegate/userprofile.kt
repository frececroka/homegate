package ch.homegate

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

data class UserProfile(
    val queryConstraints: QueryConstraints = QueryConstraints(),
    val airtableCredentials: AirtableCredentials? = null
)

data class QueryConstraints(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val minSpace: Int? = null,
    val maxSpace: Int? = null,
    val areas: List<String> = listOf(),
)

data class AirtableCredentials(
    val apiKey: String = "",
    val appId: String = ""
)

interface UserProfileRepository {
    fun get(chatId: Long): UserProfile
    fun getAll(): List<Pair<Long, UserProfile>>
    fun update(chatId: Long, update: (UserProfile) -> UserProfile)
}

@Component
@Profile("local")
@Suppress("unused")
class LocalUserProfileRepository(
    @Qualifier("profiles-db") private val db: JsonDb
) : UserProfileRepository {

    override fun get(chatId: Long): UserProfile =
        db.get<UserProfile>(chatId.toString(), UserProfile::class.java) ?: UserProfile()

    override fun getAll(): List<Pair<Long, UserProfile>> {
        return db.findAll<UserProfile>({ true }, UserProfile::class.java)
            .map { (chatId, profile) -> Pair(chatId.toLong(), profile) }
    }

    override fun update(chatId: Long, update: (UserProfile) -> UserProfile) {
        db.set(chatId.toString(), update(get(chatId)))
    }

}

@Component
@Profile("gcf")
@Suppress("unused")
class FirestoreUserProfileRepository(
    private val db: Firestore,
    @Qualifier("profiles-db") private val collection: CollectionReference
) : UserProfileRepository {

    override fun get(chatId: Long): UserProfile {
        val doc = documentReference(chatId)
        return fromDocument(doc.get().get())
    }

    override fun getAll(): List<Pair<Long, UserProfile>> =
        collection.listDocuments().map {
            val profiles = fromDocument(it.get().get())
            Pair(it.id.toLong(), profiles)
        }

    override fun update(chatId: Long, update: (UserProfile) -> UserProfile) {
        val doc = documentReference(chatId)
        db.runTransaction {
            val constraints = fromDocument(it.get(doc).get())
            val updatedConstraints = update(constraints)
            it.set(doc, updatedConstraints)
        }.get()
    }

    private fun fromDocument(doc: DocumentSnapshot) =
        doc.toObject(UserProfile::class.java) ?: UserProfile()

    private fun documentReference(chatId: Long) = collection.document(chatId.toString())

}

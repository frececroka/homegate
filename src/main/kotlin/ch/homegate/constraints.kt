package ch.homegate

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

data class QueryConstraints(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val minSpace: Int? = null,
    val maxSpace: Int? = null,
    val areas: List<String> = listOf(),
)

interface QueryConstraintsRepository {
    fun get(chatId: Long): QueryConstraints
    fun getAll(): List<Pair<Long, QueryConstraints>>
    fun update(chatId: Long, update: (QueryConstraints) -> QueryConstraints)
}

@Component
@Profile("local")
@Suppress("unused")
class LocalQueryConstraintsRepository(
    @Qualifier("query-constraints-db") private val db: JsonDb
) : QueryConstraintsRepository {

    override fun get(chatId: Long): QueryConstraints =
        db.get<QueryConstraints>(chatId.toString(), QueryConstraints::class.java) ?: QueryConstraints()

    override fun getAll(): List<Pair<Long, QueryConstraints>> {
        return db.findAll<QueryConstraints>({ true }, QueryConstraints::class.java)
            .map { (chatId, queryConstraints) -> Pair(chatId.toLong(), queryConstraints) }
    }

    override fun update(chatId: Long, update: (QueryConstraints) -> QueryConstraints) {
        val constraints = get(chatId)
        val updatedConstraints = update(constraints)
        db.set(chatId.toString(), updatedConstraints)
    }

}

@Component
@Profile("gcf")
@Suppress("unused")
class FirestoreQueryConstraintsRepository(
    private val db: Firestore,
    @Qualifier("query-constraints-db") private val collection: CollectionReference
) : QueryConstraintsRepository {

    override fun get(chatId: Long): QueryConstraints {
        val doc = documentReference(chatId)
        return fromDocument(doc.get().get())
    }

    override fun getAll(): List<Pair<Long, QueryConstraints>> =
        collection.listDocuments().map {
            val queryConstraints = fromDocument(it.get().get())
            Pair(it.id.toLong(), queryConstraints)
        }

    override fun update(chatId: Long, update: (QueryConstraints) -> QueryConstraints) {
        val doc = documentReference(chatId)
        db.runTransaction {
            val constraints = fromDocument(it.get(doc).get())
            val updatedConstraints = update(constraints)
            it.set(doc, updatedConstraints)
        }.get()
    }

    private fun fromDocument(doc: DocumentSnapshot) =
        doc.toObject(QueryConstraints::class.java) ?: QueryConstraints()

    private fun documentReference(chatId: Long) = collection.document(chatId.toString())

}

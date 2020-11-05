package ch.homegate.airtable

import ch.homegate.crawler.ListingResponse
import io.ktor.util.*

@KtorExperimentalAPI
class AirtableBackend(apiKey: String, appId: String) {

    private val airtable = AirtableClient(apiKey, appId)
    private val table = AirtableListingsTable(airtable)

    suspend fun add(result: ListingResponse) {
        val listing = result.listing
        val airtableListing = AirtableListing(
            address = listing.address.toString(),
            zip = listing.address.postalCode,
            gross_rent =  listing.prices.rent.gross,
            floor_space = listing.characteristics.space(),
            state = "Initial",
            url = result.url,
        )
        table.create(listOf(NewRecord(airtableListing)))
    }

    suspend fun delete(id: String) {
        val record = findListing(id)
        if (record != null) {
            table.delete(record.id)
        }
    }

    suspend fun setState(id: String, state: State) {
        val record = findListing(id) ?: throw IllegalArgumentException()
        val patchRecord = Record(id = record.id, fields = AirtableListingPatch(state = state.name))
        table.update(listOf(patchRecord))
    }

    private suspend fun findListing(id: String) = table.findOne(constructFilter(id))

    private fun constructFilter(id: String): Expression {
        return Expression.Search(Expression.S(id), Expression.V("url"))
    }

}

package ch.homegate.airtable

import ch.homegate.client.http.ListingResponse
import io.ktor.util.*
import java.net.URI

@KtorExperimentalAPI
class AirtableBackend(apiKey: String, appId: String) {

    private val airtable = AirtableClient(apiKey, appId)
    private val table = AirtableListingsTable(airtable)

    suspend fun add(result: ListingResponse) {
        val airtableListing = mapListing(result)
        table.create(listOf(NewRecord(airtableListing)))
    }

    private fun mapListing(result: ListingResponse): AirtableListing {
        val listing = result.listing
        val address = listing.address.toString()
        val zip = listing.address.postalCode
        val grossRent = listing.prices.rent.gross
        val floorSpace = listing.characteristics.space()
        val state = "Initial"
        val url = result.url
        val attachments = listing.localization.de.attachments
            .map { AirtableAttachment(URI(it.url)) }
        return AirtableListing(address, zip, grossRent, floorSpace, state, url, attachments)
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

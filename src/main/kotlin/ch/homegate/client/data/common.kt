package ch.homegate.client.data

data class IntRange(
    val from: Int? = null,
    val to: Int? = null,
)

data class CollectionResponse<T>(
    val from: Int,
    val size: Int,
    val total: Int,
    val results: List<T>,
)

data class Prices(
    val rent: Price,
)

data class Price(
    val gross: Int,
)

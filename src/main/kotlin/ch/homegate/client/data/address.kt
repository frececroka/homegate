package ch.homegate.client.data

data class Address(
    val country: String? = null,
    val locality: String? = null,
    val postalCode: String? = null,
    val region: String? = null,
    val street: String? = null,
) {
    override fun toString(): String {
        return "$street, $postalCode $locality"
    }
}

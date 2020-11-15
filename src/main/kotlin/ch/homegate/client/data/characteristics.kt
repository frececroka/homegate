package ch.homegate.client.data

data class Characteristics(
    val numberOfRooms: Double? = null,
    val totalFloorSpace: Int? = null,
    val livingSpace: Int? = null,
    val floor: Int? = null,
) {
    fun space() = livingSpace ?: totalFloorSpace

    override fun toString(): String {
        return listOfNotNull(
            numberOfRooms?.let { "$it rooms" },
            (space())?.let { "$it sqm" },
            floor?.let { "${it + 1}-th floor" },
        ).joinToString(", ")
    }
}

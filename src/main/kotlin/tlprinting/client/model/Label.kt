package tlprinting.client.model

import kotlinx.serialization.Serializable


@Serializable
data class Label(
    val id: String,
    val title: String,
)
package tlprinting.client.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T> (
    val data: List<T>,
    val page: PaginationData
)

@Serializable
data class PaginationData(
    val size: Int,
    val hasNext: Boolean,
    val nextCursor: String?,
)
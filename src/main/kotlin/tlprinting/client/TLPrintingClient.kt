package tlprinting.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendAll
import tlprinting.client.model.Label
import tlprinting.client.configuration.PDFConfiguration
import tlprinting.client.configuration.PrintCodeConfiguration
import tlprinting.client.model.PaginatedResponse

/**
 * Client for interacting with the TLPrinting Label Printer API.
 *
 * <p>This client provides basic methods for authenticating and interacting
 * with TLPrinting resources such as labels</p>
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * val client = new TlPrintingClient("your-api-token")
 *
 * client.getLabels()
 * }</pre>
 */
class TLPrintingClient(
    /**
     * API key. Visit (TLPrinting)[https://tlprinting.net] to get yours
     */
    private val apiKey: String,
    private val endpoint: String = DEFAULT_ENDPOINT
) {
    private val client: HttpClient = getHttpClient(apiKey)

    companion object {
        const val DEFAULT_ENDPOINT = "https://tlprinting.net"
        const val API_BASE_PATH = "api/v1"
        const val LABELS_PATH = "labels"
        const val PDF_PATH = "pdf"
        const val PRINTCODE_PATH = "printcode"

        /**
         * Create the http client
         */
        private fun getHttpClient(apiKey: String): HttpClient =
            HttpClient(CIO) {
                install(Auth) {
                    bearer {
                        BearerTokens(apiKey, null)
                    }
                }
                install(ContentNegotiation) {
                    json()
                }
            }
    }

    /**
     * List labels of a users
     * @param pageSize Number of elements to return in a page
     * @param cursor The cursor returned from the previous list request. Used for pagination
     */
    suspend fun listLabels(pageSize: Int? = null, cursor: String? = null): PaginatedResponse<Label> {
        return client.get {
            tlPrintingUrl(LABELS_PATH) {
                pageSize?.let { parameters.append("pageSize", it.toString()) }
                cursor?.let { parameters.append("cursor", it) }
            }
        }.body()
    }

    /**
     * Get the generated PDF for a label
     */
    suspend fun getLabelPDF(labelId: String, config: PDFConfiguration): ByteArray {
        return client.get {
            tlPrintingUrl(LABELS_PATH, labelId, PDF_PATH) {
                headers.append("Accept", "application/pdf")
                parameters.appendAll(config.params)
            }
        }.body()
    }

    /**
     * Generate print code for the label
     */
    suspend fun getLabelPrintCode(labelId: String, config: PrintCodeConfiguration): String {
        return client.get {
            tlPrintingUrl(LABELS_PATH, labelId, PRINTCODE_PATH, config.language.languageId) {
                parameters.appendAll(config.params)
            }
        }.body()
    }

    private fun HttpRequestBuilder.tlPrintingUrl(
        vararg path: String,
        block: URLBuilder.() -> Unit = {}
    ) {
        url {
            takeFrom(endpoint)
            appendPathSegments(API_BASE_PATH, *path)
            block()
        }
    }
}
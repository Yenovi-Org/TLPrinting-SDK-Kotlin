package net.tlprinting.client

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import net.tlprinting.client.model.Label
import net.tlprinting.client.configuration.PDFConfiguration
import net.tlprinting.client.configuration.PrintCodeConfiguration
import net.tlprinting.client.model.PaginatedResponse

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
                expectSuccess = true
                install(Auth) {
                    bearer {
                        loadTokens {
                            BearerTokens(apiKey, null)
                        }
                        sendWithoutRequest { true }
                    }
                }
                install(ContentNegotiation) {
                    json()
                }
            }
    }

    /**
     * Get metadata of a single label
     */
    suspend fun getLabel(id: String): Label {
        return client.get {
            tlPrintingUrl(LABELS_PATH, id)
        }.body()
    }

    /**
     * List labels of a users
     * @param pageSize Number of elements to return in a page
     * @param cursor The cursor returned from the previous list request. Used for pagination
     */
    suspend fun listLabels(pageSize: Int? = null, cursor: String? = null, search: String? = null): PaginatedResponse<Label> {
        return client.get {
            tlPrintingUrl(LABELS_PATH) {
                pageSize?.let { parameters.append("pageSize", it.toString()) }
                cursor?.let { parameters.append("cursor", it) }
                search?.let { parameters.append("search", it) }
            }
        }.body()
    }

    /**
     * Get the generated PDF for a label
     */
    suspend fun getLabelPDF(labelId: String, config: PDFConfiguration): ByteArray {
        return client.post {
            tlPrintingUrl(LABELS_PATH, labelId, PDF_PATH)
            headers.append("Accept", "application/pdf")
            contentType(ContentType.Application.Json)
            setBody(config.body)
        }.body()
    }

    /**
     * Generate raw print code bytes for the label
     */
    suspend fun getLabelPrintCode(labelId: String, config: PrintCodeConfiguration): ByteArray {
        return client.post {
            tlPrintingUrl(LABELS_PATH, labelId, PRINTCODE_PATH, config.language.languageId)
            contentType(ContentType.Application.Json)
            setBody(config.body)
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

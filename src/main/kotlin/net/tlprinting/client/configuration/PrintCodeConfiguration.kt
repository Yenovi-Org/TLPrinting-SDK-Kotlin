package net.tlprinting.client.configuration

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

enum class PrintCodeLanguage(val languageId: String) {
    TSPL("tspl"),
    ZPL("zpl"),
}

data class PrintCodeConfiguration(
    val language: PrintCodeLanguage,
    val copies: Int,
    /**
     * Space between rows in mm
     */
    val rowGap: Double? = null,
    /**
     * DPI to configure the printer to
     */
    val dpi: Double? = null,
    /**
     * Variables to replace on the label.
     * Each variable should follow this format MY_VAR.
     * All upper case, only letters and underscore
     */
    val variables: Map<String, String>? = null,
) {
    /**
     * JSON body sent to the print code endpoint
     */
    val body: JsonObject
        get() = buildJsonObject {
            put("copies", copies)
            rowGap?.let { put("rowGap", it) }
            dpi?.let { put("dpi", it) }
            variables?.let { vars ->
                putJsonObject("variables") {
                    vars.forEach { (key, value) -> put(key, value) }
                }
            }
        }
}

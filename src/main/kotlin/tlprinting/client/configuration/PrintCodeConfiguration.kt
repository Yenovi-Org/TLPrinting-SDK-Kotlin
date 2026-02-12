package tlprinting.client.configuration

import kotlin.collections.component1
import kotlin.collections.component2

enum class PrintCodeLanguage(val languageId: String) {
    TSPL("tspl")
}

data class PrintCodeConfiguration(
    val language: PrintCodeLanguage = PrintCodeLanguage.TSPL,
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
    val params: Map<String, String>
        get() = buildMap {
            put("copies", copies.toString())

            rowGap?.let { put("rowGap", it.toString()) }
            dpi?.let { put("dpi", it.toString()) }

            variables?.forEach { (key, value) ->
                put("var_$key", value)
            }
        }
}

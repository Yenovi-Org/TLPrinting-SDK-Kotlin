package tlprinting.client.configuration

enum class PageSize(val id: String) {
    LABEL("label"),
    CUSTOM("custom"),
    A3("A3"),
    A4("A4"),
    A5("A5"),
    A6("A6")
}

data class PDFConfiguration(
    val pageSize: PageSize = PageSize.LABEL,
    /**
     * Horizontal margin in mm
     */
    val marginX: Double? = null,
    /**
     * Vertical margin in mm
     */
    val marginY: Double? = null,
    /**
     * Spacing between columns in mm
     */
    val columnGap: Double? = null,
    /**
     * Spacing between rows in mm
     */
    val rowGap: Double? = null,
    /**
     * Number of labels to print
     */
    val copies: Int,
    /**
     * Page height in mm. Only used if `pageSize` is `custom`
     */
    val pageHeight: Double? = null,
    /**
     * Page width in mm. Only used if `pageSize` is `custom`
     */
    val pageWidth: Double? = null,
    /**
     * Variables to replace on the label.
     * Each variable should follow this format MY_VAR.
     * All upper case, only letters and underscore
     */
    val variables: Map<String, String>? = null,
) {
    val params: Map<String, String>
        get() = buildMap {
            put("pageSize", pageSize.name)
            put("copies", copies.toString())

            marginX?.let { put("marginX", it.toString()) }
            marginY?.let { put("marginY", it.toString()) }
            columnGap?.let { put("columnGap", it.toString()) }
            rowGap?.let { put("rowGap", it.toString()) }
            pageHeight?.let { put("pageHeight", it.toString()) }
            pageWidth?.let { put("pageWidth", it.toString()) }

            variables?.forEach { (key, value) ->
                put("var_$key", value)
            }
        }
}
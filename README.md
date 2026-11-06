# TLPrinting Kotlin SDK

Kotlin client for the [TLPrinting](https://tlprinting.net) label printing API. Use it to list your labels, fetch label metadata, render labels to PDF, and generate raw printer code (TSPL or ZPL).

- Coroutine-based (`suspend` functions), built on Ktor and kotlinx.serialization
- JVM 17+
- Bearer token authentication

## Installation

The SDK is published to GitHub Packages. GitHub Packages requires authentication even for reading public packages, so you need a GitHub personal access token with the `read:packages` scope.

**Gradle (Kotlin DSL)**

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/Yenovi-Org/TLPrinting-SDK-Kotlin")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("net.tlprinting:tlprinting-sdk-kotlin:<version>")
}
```

Replace `<version>` with a release tag from the [Releases/Packages](https://github.com/Yenovi-Org/TLPrinting-SDK-Kotlin/packages) page. The artifact ID follows the project name in `settings.gradle.kts`.

## Getting started

Get an API key from [tlprinting.net](https://tlprinting.net), then create a client:

```kotlin
import net.tlprinting.client.TLPrintingClient

val client = TLPrintingClient(apiKey = "your-api-key")
```

To target a different server (for example staging), pass `endpoint`:

```kotlin
val client = TLPrintingClient(apiKey = "your-api-key", endpoint = "https://staging.example.com")
```

All client methods are `suspend` functions, so call them from a coroutine.

## Usage

### Get a label

```kotlin
val label = client.getLabel("label-id")
println("${label.id}: ${label.title}")
```

### List labels

Results are cursor-paginated. Pass the `nextCursor` from one page to get the next.

```kotlin
var cursor: String? = null
do {
    val page = client.listLabels(pageSize = 50, cursor = cursor, search = "shipping")
    page.data.forEach { println(it.title) }
    cursor = page.page.nextCursor
} while (page.page.hasNext)
```

All three parameters (`pageSize`, `cursor`, `search`) are optional.

### Generate a PDF

```kotlin
import net.tlprinting.client.configuration.PDFConfiguration
import net.tlprinting.client.configuration.PageSize

val pdf: ByteArray = client.getLabelPDF(
    labelId = "label-id",
    config = PDFConfiguration(
        pageSize = PageSize.A4,
        copies = 10,
        marginX = 5.0,
        marginY = 5.0,
        columnGap = 2.0,
        rowGap = 2.0,
    ),
)

File("labels.pdf").writeBytes(pdf)
```

`PDFConfiguration` options:

| Option | Type | Description |
| --- | --- | --- |
| `pageSize` | `PageSize` | `LABEL` (default), `CUSTOM`, `A3`, `A4`, `A5`, `A6` |
| `copies` | `Int` | Number of labels to print (required) |
| `marginX`, `marginY` | `Double?` | Horizontal and vertical page margins in mm |
| `columnGap`, `rowGap` | `Double?` | Spacing between columns and rows in mm |
| `pageWidth`, `pageHeight` | `Double?` | Page size in mm. Only used with `PageSize.CUSTOM` |
| `variables` | `Map<String, String>?` | Values for label variables (see below) |

### Generate printer code

Returns the raw bytes to send to a thermal printer.

```kotlin
import net.tlprinting.client.configuration.PrintCodeConfiguration
import net.tlprinting.client.configuration.PrintCodeLanguage

val code: ByteArray = client.getLabelPrintCode(
    labelId = "label-id",
    config = PrintCodeConfiguration(
        language = PrintCodeLanguage.ZPL,
        copies = 2,
        dpi = 203.0,
    ),
)
```

`PrintCodeConfiguration` options:

| Option | Type | Description |
| --- | --- | --- |
| `language` | `PrintCodeLanguage` | `TSPL` or `ZPL` (required) |
| `copies` | `Int` | Number of labels to print (required) |
| `rowGap` | `Double?` | Space between rows in mm |
| `dpi` | `Double?` | DPI to configure the printer to |
| `variables` | `Map<String, String>?` | Values for label variables (see below) |

### Label variables

Labels can contain placeholders that you fill in at print time. Variable names must be upper case, letters and underscores only (for example `CUSTOMER_NAME`). Pass them in `variables` for both PDF and print code requests:

```kotlin
PDFConfiguration(
    copies = 1,
    variables = mapOf(
        "CUSTOMER_NAME" to "Jane Doe",
        "ORDER_ID" to "A-1042",
    ),
)
```

## Error handling

The client is configured with `expectSuccess = true`, so any non-2xx response throws a Ktor exception (`ClientRequestException` for 4xx, `ServerResponseException` for 5xx). Catch these to inspect the status code and response:

```kotlin
import io.ktor.client.plugins.ClientRequestException

try {
    client.getLabel("missing")
} catch (e: ClientRequestException) {
    println("Request failed: ${e.response.status}")
}
```

## Development

Requirements: JDK 17.

```bash
./gradlew build   # compile
./gradlew test    # run tests
```

Project layout:

```
src/main/kotlin/net/tlprinting/client/
├── TLPrintingClient.kt     # API client
├── configuration/          # PDFConfiguration, PrintCodeConfiguration
└── model/                  # Label, PaginatedResponse
```

## Releasing

Releases are tag-driven. Pushing a tag that starts with a digit runs the [release workflow](.github/workflows/release.yml), which runs the tests and, if they pass, publishes the package to GitHub Packages. The tag name is used as the version.

```bash
git checkout main && git pull
git tag 0.0.8
git push origin 0.0.8
```

There is no version file to bump. Locally the version defaults to `0.1`.

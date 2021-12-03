import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Check Java 11 java.net.http module
 */
val client = HttpClient.newHttpClient()
val request = HttpRequest.newBuilder()
    .uri(URI("https://httpstat.us/418"))
    .header("accept", "*/*")
    .build()
val response = client.send(request, HttpResponse.BodyHandlers.ofString())
println("${response.statusCode()}| ${response.body()}")

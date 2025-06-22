import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun fetchJwtToken(): String? {
    val email = "dvu3999@gmail.com"
    val password = "Dochet1989"
    val client = HttpClient(CIO)
    return try {
        val response: HttpResponse = client.post("https://thingsboard.cloud/api/auth/login") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody("""{"username":"$email", "password":"$password"}""")
        }


        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            val response= Json.parseToJsonElement(responseBody).jsonObject
            Log.d("MainActivity", "Login thành công: $responseBody")
            val token = response.jsonObject["token"]?.jsonPrimitive?.content ?: "Unknown"
            token// đây là chuỗi JSON chứa token và refreshToken
        } else {
            Log.e("MainActivity", "Login lỗi: ${response.status}")
            null
        }
    } catch (e: Exception) {
        Log.e("MainActivity", "Lỗi khi gọi AP", e)
        null
    } finally {
        client.close()
    }
}

package com.example.project2_verse3.controller
import android.util.Log
import com.example.project2_verse3.greatToken
import com.example.project2_verse3.model.mainUser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class AssetResponse(
    val id: AssetId,
    val name: String,
    val type: String
)

@Serializable
data class AssetId(
    val entityType: String,
    val id: String
)


val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
}
fun createBossAsset(
    bossName: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = httpClient.post("https://thingsboard.cloud/api/asset") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $greatToken")
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                }
                setBody(JsonObject(mapOf(
                    "name" to JsonPrimitive(bossName),
                    "type" to JsonPrimitive("default"),
                    "customerId" to  JsonObject(mapOf(
                        "entityType" to JsonPrimitive("CUSTOMER"),
                        "id"  to JsonPrimitive(mainUser.cusID)
                    )),
                    "additionalInfo" to JsonObject(mapOf(
                        "description" to JsonPrimitive("ESP32_AP/12345678")
                    ))

                )))
            }

            if (response.status.isSuccess()) {
                val responseBody = response.body<AssetResponse>()
                val assetId = responseBody.id.id
                withContext(Dispatchers.Main) {
                        onSuccess(assetId)
                        Log.d("MainActivity", "Asset created and assigned: $assetId")
                    }

            } else {
                withContext(Dispatchers.Main) {
                    onError("HTTP ${response.status.value}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
fun createStaffDevice(
    staffName: String,
    treeNumber: Int,
    bossAssetId: String,
    onSuccess: (String, String) -> Unit, // trả về deviceId và accessToken
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // B1: Tạo device mới
            val createResponse = httpClient.post("https://thingsboard.cloud/api/device") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $greatToken")
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                }
                setBody(JsonObject(mapOf(
                    "name" to JsonPrimitive("$staffName "),
                    "type" to JsonPrimitive("default"),
                    "label" to JsonPrimitive("$treeNumber"),
                    "customerId" to JsonObject(mapOf(
                        "entityType" to JsonPrimitive("CUSTOMER"),
                        "id" to JsonPrimitive(mainUser.cusID)
                    ))

                )))
            }

            if (createResponse.status.isSuccess()) {
                val responseBody = createResponse.body<AssetResponse>()
                val deviceId = responseBody.id.id

                // B2: Lấy credentials của device
                val credentialsResponse = httpClient.get("https://thingsboard.cloud/api/device/$deviceId/credentials") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $greatToken")
                    }
                }

                if (credentialsResponse.status.isSuccess()) {
                    val credentialsBody = credentialsResponse.body<JsonObject>()
                    val accessToken = credentialsBody["credentialsId"]?.jsonPrimitive?.content ?: ""

                    // B3: Tạo quan hệ device vào asset (bossAssetId)
                    val relationResponse = httpClient.post("https://thingsboard.cloud/api/relation") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $greatToken")
                            append(HttpHeaders.ContentType, ContentType.Application.Json)
                        }
                        setBody(JsonObject(mapOf(
                            "from" to JsonObject(mapOf(
                                "entityType" to JsonPrimitive("ASSET"),
                                "id" to JsonPrimitive(bossAssetId)
                            )),
                            "to" to JsonObject(mapOf(
                                "entityType" to JsonPrimitive("DEVICE"),
                                "id" to JsonPrimitive(deviceId)
                            )),
                            "type" to JsonPrimitive("Contains"),
                            "typeGroup" to JsonPrimitive("COMMON")
                        )))
                    }

                    if (relationResponse.status.isSuccess()) {
                        withContext(Dispatchers.Main) {
                            onSuccess(deviceId, accessToken)

                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("Failed to create relation: HTTP ${relationResponse.status.value}")
                        }
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        onError("Failed to get credentials: HTTP ${credentialsResponse.status.value}")
                    }
                }

            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed to create device: HTTP ${createResponse.status.value}")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}





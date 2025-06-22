package com.example.project2_verse3.controller

import android.util.Log
import com.example.project2_verse3.R
import com.example.project2_verse3.greatToken
import com.example.project2_verse3.model.UserModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

///api/customerInfos/all{?pageSize,page,includeCustomers,textSearch,sortProperty,sortOrder}
suspend fun fetchUserList(): List<UserModel> {
    val client = HttpClient(CIO)
    val token = greatToken
    val assetUrl = "https://thingsboard.cloud/api/customerInfos/all?pageSize=100&page=0"

    val resultList = mutableListOf<UserModel>()

    try {
        val response: HttpResponse = client.get(assetUrl) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (response.status.value != 200) {
            Log.e("MainActivity", "Lỗi khi gọi API: ${response.status}")
            Log.e("MainActivity", "Response body: ${response.bodyAsText()}")
            Log.e("MainActivity", greatToken)
        }

        val responseBody = response.bodyAsText()

        val assetJson = Json.parseToJsonElement(responseBody).jsonObject
        val assetList = assetJson["data"]?.jsonArray ?: JsonArray(emptyList())

        for (asset in assetList) {
            val id = asset.jsonObject["id"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: continue
            val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: "Unknown"
            val gmail = asset.jsonObject["email"]?.jsonPrimitive?.content ?: "Unknown"
            val password = asset.jsonObject["additionalInfo"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: "continue"

            resultList.add(UserModel(gmail, name, id, R.drawable.tree_, password))
        }

    } catch (e: Exception) {
        Log.e("getUserList", "Exception: ${e.message}", e)
    } finally {
        client.close()
    }

    return resultList
}

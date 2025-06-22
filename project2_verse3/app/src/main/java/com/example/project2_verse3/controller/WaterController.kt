package com.example.project2_verse3.controller

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import bossListAll
import com.example.project2_verse3.greatToken
import com.example.project2_verse3.model.NotiModel
import com.example.project2_verse3.model.StaffModel
import com.example.project2_verse3.ui.navigation.addNotiToHashStore
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.Instant


@RequiresApi(Build.VERSION_CODES.O)
fun PublishTelemetry(context: Context, device: StaffModel,command : String,cmdValue : Int =0, onComplete: (Boolean) -> Unit) {
    val boss = (bossListAll.find { it.BossID == device.BossID })
    val bossName = boss!!.bossName
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val credentialsResponse = httpClient.get("https://thingsboard.cloud/api/device/${device.staffID}/credentials") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $greatToken")
                }
            }

            if (credentialsResponse.status.isSuccess()) {
                val credentialsBody = credentialsResponse.body<JsonObject>()
            //    val accessToken = credentialsBody["credentialsId"]?.jsonPrimitive?.content ?: ""


            val response = httpClient.post("https://thingsboard.cloud/api/rpc/oneway/031c7760-2ede-11f0-ac07-7d443117f3f7") {
                val payload = buildJsonObject {
                    put("method", device.BossID)
                    putJsonObject("params") {
                        put("bossID", bossName)
                        put("device",device.staffID)
                        put("command", command)
                        put("cmdValue", cmdValue)


                    }
                    put("timeout", 20000)
                }
                contentType(ContentType.Application.Json)
                header("X-Authorization", "Bearer $greatToken")
                setBody(payload)

            }
            if (response.status.isSuccess()) {
                addNotiToHashStore(context,
                    NotiModel(bossName, device.tree.name, 1, Instant.now().toEpochMilli())
                )
                Log.d("RPC", "Sent successfully!")
                withContext(Dispatchers.Main) { onComplete(true) }
            } else {
                addNotiToHashStore(context,NotiModel(bossName, device.tree.name, 1,Instant.now().toEpochMilli()))
                Log.e("RPC", "Failed: ${response.status.value}")
                Log.e("RPC", "Failed: ${response.bodyAsText()}")
                Log.e("RPC", device.staffID)

                withContext(Dispatchers.Main) { onComplete(false) }
            }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RPC", "Exception: ${e.localizedMessage}")
            withContext(Dispatchers.Main) { onComplete(false) }
        }
    }
}

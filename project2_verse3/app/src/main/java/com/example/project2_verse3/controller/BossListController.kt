import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.project2_verse3.greatToken
import com.example.project2_verse3.model.BossModel
import com.example.project2_verse3.model.NotiModel
import com.example.project2_verse3.model.StaffModel

import com.example.project2_verse3.model.treeList
import com.example.project2_verse3.ui.navigation.WifiInfo
import com.example.project2_verse3.ui.navigation.addNotiToHashStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

var staffListAll = mutableListOf<StaffModel>()
var bossListAll = mutableListOf<BossModel>()

suspend fun getBossListId(customerId: String): List<BossModel> {
    val client = HttpClient(CIO)
    val token = greatToken
    val assetUrl = "https://thingsboard.cloud/api/customer/$customerId/assetInfos?pageSize=100&page=0"
    val bossList = mutableListOf<BossModel>()

    try {
        val response: HttpResponse = client.get(assetUrl) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $greatToken")
            }
        }
        // Kiểm tra status code
        if (response.status.value != 200) {
            Log.e("MainActivity", "Lỗi khi gọi APII: ${response.status}")
            Log.e("MainActivity", "Response body: ${response.bodyAsText()}")
            return bossList
        }

        val responseBody = response.bodyAsText()
        Log.d("MainActivity", "Response body: $responseBody")

        val assetJson = Json.parseToJsonElement(responseBody).jsonObject
        val assetList = assetJson["data"]?.jsonArray ?: JsonArray(emptyList())
        Log.d("MainActivity", "Response body: $assetList")

        for (asset in assetList) {
            Log.d("MainActivity", "Response body: $asset")
            val id = asset.jsonObject["id"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: continue
            val name = asset.jsonObject["name"]?.jsonPrimitive?.content ?: "Unknown"
            val wifInfo=asset.jsonObject["additionalInfo"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: "continue"
            val wifiSSID=wifInfo.split("/")

            val staffList = getDeviceList(id)
            Log.d("MainActivity", "1")
            bossList.add(BossModel(id,name, WifiInfo(wifiSSID[0],wifiSSID[1],"null"), 1, staffList))
            Log.d("MainActivity", "2")
        }

    } catch (e: Exception) {
        Log.e("getBossListId", "Exception: ${e.message}", e)
    } finally {
        client.close()
    }
    bossListAll=bossList
    return bossList
}


suspend fun getDeviceList(bossId: String): List<StaffModel> {
    val client = HttpClient(CIO)
    val token = greatToken
    val staffList = mutableListOf<StaffModel>()

    try {
        val relationUrl =
            "https://thingsboard.cloud/api/relations?fromId=$bossId&fromType=ASSET&relationType=Contains&relationTypeGroup=COMMON"

        val relationResponse: HttpResponse = client.get(relationUrl) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (relationResponse.status.value != 200) {
            Log.e("getDeviceList", "Lỗi khi lấy relations: ${relationResponse.status}")
            Log.e("getDeviceList", relationResponse.bodyAsText())
            return staffList
        }

        val relationData = Json.parseToJsonElement(relationResponse.bodyAsText()).jsonArray

        for (relation in relationData) {
            val deviceId =
                relation.jsonObject["to"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: continue

            // Lấy thông tin thiết bị
            val deviceInfoResponse = client.get("https://thingsboard.cloud/api/device/$deviceId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (deviceInfoResponse.status.value != 200) {
                Log.e("getDeviceList", "Lỗi khi lấy thông tin thiết bị: ${deviceInfoResponse.status}")
                Log.e("getDeviceList", deviceInfoResponse.bodyAsText())
                continue
            }

            val deviceInfo = Json.parseToJsonElement(deviceInfoResponse.bodyAsText()).jsonObject
            val label = deviceInfo["label"]?.jsonPrimitive?.int ?: 0

            // Lấy telemetry
            val telemetryUrl =
                "https://thingsboard.cloud/api/plugins/telemetry/DEVICE/$deviceId/values/timeseries?keys=value"

            val telemetryResponse = client.get(telemetryUrl) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (telemetryResponse.status.value != 200) {
                Log.e("getDeviceList", "Lỗi khi lấy telemetry: ${telemetryResponse.status}")
                Log.e("getDeviceList", telemetryResponse.bodyAsText())
                continue
            }

            val telemetry =
                Json.parseToJsonElement(telemetryResponse.bodyAsText()).jsonObject
            val humidity =
                telemetry["value"]?.jsonArray?.getOrNull(0)?.jsonObject?.get("value")?.jsonPrimitive?.floatOrNull?.toInt()
                    ?: 0

            val dummyTree = treeList[label]
            staffList.add(StaffModel(BossID = bossId, staffID = deviceId , Humidity = humidity, tree = dummyTree))

        }

    } catch (e: Exception) {
        Log.e("getDeviceList", "Exception: ${e.message}", e)
    } finally {
        client.close()
    }
    staffListAll= staffList
    return staffList
}


@Composable
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition")
fun crawlList(deviceList : List<StaffModel>){

    val scope = rememberCoroutineScope()
    val context= LocalContext.current
    for (device in deviceList){
        val boss = (bossListAll.find { it.BossID == device.BossID })
        val bossName = boss!!.bossName
        if(device.Humidity< device.tree.RecommendHumidity){
            scope.launch {
                addNotiToHashStore(context, NotiModel(bossName,device.tree.name,2, Instant.now().toEpochMilli()))
            }
        }
    }

}


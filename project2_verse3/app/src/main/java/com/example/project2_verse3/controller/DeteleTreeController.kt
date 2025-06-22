package com.example.project2_verse3.controller


import com.example.project2_verse3.greatToken
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


suspend fun DeteleTreeController(
    deviceId: String,
    confirm: (Unit) -> Boolean,
    noti: (String) -> Unit
) {
    withContext(Dispatchers.Main) {
        if (!confirm(Unit)) {
            noti("Đã hủy xóa thiết bị.")
            return@withContext
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = httpClient.delete("https://thingsboard.cloud/api/device/$deviceId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $greatToken")
                }
            }

            withContext(Dispatchers.Main) {
                if (response.status.isSuccess()) {
                    noti("Xóa thiết bị thành công.")

                } else {
                    noti("Xóa thiết bị thất bại: ${response.status.value}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                noti("Lỗi khi xóa thiết bị: ${e.localizedMessage}")
            }
        }
    }
}



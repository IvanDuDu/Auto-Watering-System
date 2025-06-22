package com.example.project2_verse3.controller


import bossListAll
import com.example.project2_verse3.greatToken
import com.example.project2_verse3.model.BossModel
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


suspend fun DeteleBossController(
    boss: BossModel,
    confirm: (Unit) -> Boolean,
    noti: (String) -> Unit
) {
    withContext(Dispatchers.Main) {
        if (!confirm(Unit)) {
            noti("Đã hủy xóa máy chủ.")
            return@withContext
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = httpClient.delete("https://thingsboard.cloud/api/asset/${boss.BossID}") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $greatToken")
                }
            }

            withContext(Dispatchers.Main) {
                if (response.status.isSuccess()) {
                    noti("Xóa máy chủ thành công.")
                    bossListAll.remove(boss)
                } else {
                    noti("Xóa máy chủ thất bại: ${response.status.value}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                noti("Lỗi khi xóa máy chủ: ${e.localizedMessage}")
            }
        }
    }
}



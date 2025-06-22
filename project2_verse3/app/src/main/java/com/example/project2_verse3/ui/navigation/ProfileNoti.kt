
package com.example.project2_verse3.ui.navigation
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.project2_verse3.model.NotiModel
import com.example.project2_verse3.model.mainUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



val Context.hashDataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = mainUser.cusName)
@Composable
fun ProfileNoti(bossName:String, staffName: String, formType:Int, time: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(color = Color(0x5086EE60), shape = RoundedCornerShape(8.dp))
            .fillMaxHeight()
            .padding(8.dp)

    ) {
        Column  {
            Text(
                bossName,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (formType == 1) {
                Text(
                    "Tưới cho cây $staffName ${time / 60} giờ ${(time) % 60} phút trước",
                    fontSize = 15.sp
                )
            } else if (formType == 2) {
                Text(
                    "$staffName ở dưới ngưỡng giới hạn , có vẻ bơm đang gặp sự cố",
                    fontSize = 15.sp
                )
            } else if (formType == 3) {
                Text(
                    "Lần cuối cùng nghe tin từ $staffName là ${time / 60} giờ ${(time) % 60} phút trước" +
                            " có thể cảm biến đã gặp nạn", fontSize = 15.sp
                )
            } else if (formType == 4) {
                Text(
                    " Đã thêm thiết bị mới",
                    fontSize = 15.sp
                )
            }

        }
    }
}

fun readNotiListFromHashStore(context: Context): Flow<List<NotiModel>> {
    return context.hashDataStore.data.map { preferences ->
        val jsonString = preferences[stringPreferencesKey("noti_list")]
        if (jsonString != null) {
            Json.decodeFromString(jsonString)
        } else {
            emptyList()
        }
    }
}


    // gọi ở cuối hàm
suspend fun saveNotiListToHashStore(context: Context, notiList: List<NotiModel>) {
    val jsonString = Json.encodeToString(notiList)
    context.hashDataStore.edit { preferences ->
        preferences[stringPreferencesKey("noti_list")] = jsonString
    }
}

    // hàm gọi ở các vị trí khác
@RequiresApi(Build.VERSION_CODES.O)
suspend fun addNotiToHashStore(context: Context, newNoti: NotiModel) {
    val currentList = readNotiListFromHashStore(context).first()
    val updatedList = currentList + newNoti
    saveNotiListToHashStore(context , updatedList)
}

suspend fun clearAllNotiFromHashStore(context: Context) {
    context.hashDataStore.edit { preferences ->
        preferences.remove(stringPreferencesKey("noti_list"))
    }
}



package com.example.project2_verse3.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project2_verse3.model.UserModel
import com.example.project2_verse3.ui.navigation.ProfileHeader
import com.example.project2_verse3.ui.navigation.ProfileNoti
import com.example.project2_verse3.ui.navigation.clearAllNotiFromHashStore
import com.example.project2_verse3.ui.navigation.readNotiListFromHashStore
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
import crawlList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import staffListAll
import java.time.Instant
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(user: UserModel, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notiList by readNotiListFromHashStore(context).collectAsState(initial = emptyList())
    val showDialog = remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        ProfileHeader(user =user,navController)


        Spacer(modifier = Modifier.height(85.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            items(notiList) { noti ->
                ProfileNoti(
                    bossName = noti.bossName,
                    staffName = noti.treeName,
                    formType = noti.formType,
                    time = (Instant.now().toEpochMilli() - noti.time) / 3600000 // 3600*1000 ms
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Button(
            colors = ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi),
            onClick = {
                coroutineScope.launch {
                    clearAllNotiFromHashStore(context)
                    delay(2000)
                }
                showDialog.value = true
            },
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
            Text(text = "Đánh dấu là đã đọc")
        }
        if(showDialog.value){
            crawlList(staffListAll)
            showDialog.value = false
        }
        crawlList(staffListAll)
    }
}





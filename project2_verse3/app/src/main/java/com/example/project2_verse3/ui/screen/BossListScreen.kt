package com.example.project2_verse3.ui.screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bossListAll
import com.example.project2_verse3.controller.DeteleBossController
import com.example.project2_verse3.controller.DeteleTreeController
import com.example.project2_verse3.model.BossModel
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BossListScreen(bossList: List<BossModel>) {
    val context= LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.overscroll(overscrollEffect = ScrollableDefaults.overscrollEffect() )
    ) {
        items(bossList) { boss ->
            TextButton(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(8.dp),
                onClick = {
                    showConfirmDialog = true
                }, colors = ButtonColors(XanhNavy, NonChuoi, Color.Gray, Color.DarkGray)
            ) {
                Text(text ="🌳 "+ boss.bossName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Xác nhận") },
                    text = { Text("Bạn có chắc muốn xóa máy chủ này không?\n " +
                            "Lưu ý máy chủ phải không kết nối với thiết bị nào ", fontSize = 15.sp)},

                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            coroutineScope.launch {
                                DeteleBossController(
                                      boss = boss,
                                    confirm = {
                                        boss.staffs.isEmpty()
                                    },
                                    noti = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                        }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            StaffListWithBottomSheet(staffList = boss.staffs)

        }
    }
}

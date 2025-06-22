package com.example.project2_verse3.ui.screen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2_verse3.model.StaffModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import bossListAll
import com.example.project2_verse3.controller.DeteleTreeController
import com.example.project2_verse3.controller.PublishTelemetry
import com.example.project2_verse3.ui.navigation.InputDialog
import com.example.project2_verse3.ui.navigation.TimeDialog
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
import staffListAll


@Composable
fun StaffItem(staff: StaffModel) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(200.dp)
                .background(Color(0xFFF4F7ED), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = staff.tree.image),
                contentDescription = staff.tree.name,
                modifier = Modifier.size(250.dp)
            )
        }
        Box(modifier = Modifier.height(10.dp))
        Text(text = staff.tree.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color =  Color(0xFF2B3752))
        Box(modifier = Modifier.height(10.dp))
        Row(){
            Text(text = "Độ ẩm : ", fontSize = 18.sp,)
            Text(
                text = "${staff.Humidity}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (staff.Humidity < 50) Color.Red else Color(0xFF86EE60)
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StaffDetailBottomSheet(staff: StaffModel) {
    val context=LocalContext.current
    var state= staff.Humidity > staff.tree.RecommendHumidity
    val coroutineScope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showtimeDialog by remember { mutableStateOf(false) }




    Column(
        modifier = Modifier.fillMaxWidth().padding(5.dp)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Chi tiết cây:", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text("🌳 Tên cây: ${staff.tree.name}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("💧 Độ ẩm khuyến nghị: ${staff.tree.RecommendHumidity}%", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(200.dp),
                    onClick = {
                    showEditDialog = true
                }, colors = ButtonColors(XanhNavy, NonChuoi, Color.Gray,Color.DarkGray)) {
                    Text("Chỉnh sửa độ ẩm")
                }
                Button(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(200.dp),
                    onClick = {
                    showConfirmDialog = true
                }, colors = ButtonColors(XanhNavy, NonChuoi, Color.Gray,Color.DarkGray)) {
                    Text("Xóa thiết bị")
                }
                Button(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(200.dp),
                    onClick = {
                        showEditDialog = true
                    }, colors = ButtonColors(XanhNavy, NonChuoi, Color.Gray,Color.DarkGray)) {
                    Text("Setting time")
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text("Xác nhận") },
                        text = { Text("Bạn có chắc muốn xóa thiết bị này không?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showConfirmDialog = false
                                coroutineScope.launch {
                                    DeteleTreeController(
                                        deviceId = staff.staffID,
                                        confirm = {
                                            true
                                        },
                                        noti = { message ->
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    staffListAll.remove(staff)
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
                if(showEditDialog) {
                    InputDialog(
                        onDismiss = { showEditDialog = false },
                        onSubmit = { inputValue ->
                             PublishTelemetry(context, staff, "UPDATE", inputValue) { success ->
                                if (success) {
                                    Toast.makeText(context, "Cập nhật chỉ số thành công",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Cập nhật chỉ số thất bại",Toast.LENGTH_SHORT).show()
                                }
                            }
                            showEditDialog = false
                        }
                    )
                }
                if(showtimeDialog) {
                    TimeDialog(
                        onDismiss = { showtimeDialog = false },
                        onSubmit = { inputValue ->
                            PublishTelemetry(context, staff, "TIME", inputValue) { success ->
                                if (success) {
                                    Toast.makeText(context, "Cập nhật chỉ số thành công",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Cập nhật chỉ số thất bại",Toast.LENGTH_SHORT).show()
                                }
                            }
                            showtimeDialog = false
                        }
                    )
                }

            }
            Image(
                painter = painterResource(id = staff.tree.image),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
        }

        Button(shape = RoundedCornerShape(40.dp),
            onClick=(
                  { PublishTelemetry(context, staff, "WATER") { success ->
                          if (success) {
                              Toast.makeText(context, "Tưới thành công thành công",Toast.LENGTH_SHORT).show()
                          } else {
                              Toast.makeText(context, "Thiết bị đang offline",Toast.LENGTH_SHORT).show()
                          }
                      }


                  }
                    ),
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp),
            colors = if (state ) {  ButtonDefaults.buttonColors(
                containerColor = XanhNavy,
                contentColor = NonChuoi) }
            else{
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEB3678),
                    contentColor = Begie) }

        ) {
            Text("Tưới ngay ", fontSize = 45.sp, fontWeight = FontWeight.ExtraBold )
        }

    }

}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListWithBottomSheet(staffList: List<StaffModel>) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStaff by remember { mutableStateOf<StaffModel?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        LazyRow {
            items(staffList) { staff ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            selectedStaff = staff
                            scope.launch { sheetState.show() }
                        }
                ) {
                    StaffItem(staff = staff)
                }
            }
        }

        if (selectedStaff != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    selectedStaff = null
                },

                sheetState = sheetState
            ) {
                StaffDetailBottomSheet(
                    staff = selectedStaff!!,
                )
            }
        }
    }
}

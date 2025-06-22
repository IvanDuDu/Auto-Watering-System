package com.example.project2_verse3.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bossListAll
import com.example.project2_verse3.controller.createBossAsset
import com.example.project2_verse3.controller.createStaffDevice
import com.example.project2_verse3.model.BossModel
import com.example.project2_verse3.model.MainViewModel
import com.example.project2_verse3.model.NotiModel
import com.example.project2_verse3.model.StaffModel
import com.example.project2_verse3.model.treeList
import com.example.project2_verse3.ui.navigation.QRScannerScreen
import com.example.project2_verse3.ui.navigation.WifiInfo
import com.example.project2_verse3.ui.navigation.addNotiToHashStore
import com.example.project2_verse3.ui.navigation.connectToWifi
import com.example.project2_verse3.ui.navigation.parseWifiQRCode
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import staffListAll
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

enum class NewDeviceStep {
    CHOOSE_ROLE,
    BOSS_FORM,
    STAFF_FORM,
    SCAN_QR,
    CONNECTING
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NewDeviceScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val currentStep = remember { mutableStateOf(NewDeviceStep.CHOOSE_ROLE) }

    val selectedRole = remember { mutableStateOf("Boss") }
    var selectedTree by remember { mutableIntStateOf(0) }
    val bossAssetId = remember { mutableStateOf("") }
    var selectedBossId by remember { mutableStateOf("") }
    val ssid = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val bossName = remember { mutableStateOf("") }
    val deviceToken = remember { mutableStateOf("") }
    val scannedValue = remember { mutableStateOf<String?>(null) }
    val progressState = remember { mutableFloatStateOf(0f) }

    // cấp quyền
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    when (currentStep.value) {
        NewDeviceStep.CHOOSE_ROLE -> {
            RoleSelectionScreen(
                selectedRole = selectedRole.value,
                onRoleSelected = { selectedRole.value = it },
                onNext = {
                    currentStep.value =
                        if (selectedRole.value == "Boss") NewDeviceStep.BOSS_FORM else NewDeviceStep.STAFF_FORM
                }
            )
        }

        NewDeviceStep.BOSS_FORM -> {
            BossFormScreen(
                onBack = { currentStep.value = NewDeviceStep.CHOOSE_ROLE },
                onSend = { inputSsid, inputPass, inputBossName ->
                    ssid.value = inputSsid
                    password.value = inputPass
                    bossName.value = inputBossName

                    createBossAsset(inputBossName,
                        onSuccess = { assetId ->
                            bossAssetId.value = assetId
                            currentStep.value = NewDeviceStep.SCAN_QR
                            bossListAll.add(BossModel(assetId,bossName.value,
                                WifiInfo( ssid.value,password.value,""),0, emptyList()
                            ))
                        },
                        onError = { error ->
                            Log.e("NewDeviceScreen", "Failed to create asset: $error")
                        }
                    )
                }
            )
        }

        NewDeviceStep.STAFF_FORM -> {
            StaffFormScreen(
                bossList = bossListAll,
                selectedBossId = selectedBossId,
                selectedTree= selectedTree,
                onBossSelected = { selectedBossId = it },
                onTreeSelected = { selectedTree = it },

                onBack = { currentStep.value = NewDeviceStep.CHOOSE_ROLE },
                onSend = { inputSsid, inputPass ->
                    ssid.value = inputSsid
                    password.value = inputPass


                    createStaffDevice(
                        staffName = selectedBossId + Math.random().toString(),
                        treeNumber = selectedTree,
                        bossAssetId = selectedBossId,
                        onSuccess = { deviceId, accessToken ->
                            bossAssetId.value= deviceId
                            deviceToken.value= accessToken
                           staffListAll.add(StaffModel(selectedBossId,deviceId,0, treeList[selectedTree]))
                            // Xử lý khi tạo xong: hiển thị, log hoặc chuyển màn hình
                            Log.d("NewDeviceScreen", "Device created: $deviceId, Token: $accessToken")
                            Toast.makeText(context, "Tạo thiết bị thành công!", Toast.LENGTH_SHORT).show()
                            currentStep.value = NewDeviceStep.SCAN_QR
                        },
                        onError = { error ->
                            Log.e("NewDeviceScreen", "Error: $error")
                            Toast.makeText(context, "Tạo thiết bị thất bại: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

            )
        }

        NewDeviceStep.SCAN_QR -> {
            if (cameraPermissionState.status.isGranted) {
                QRScannerScreen(onResult = { result ->
                    progressState.value=0.1f
                    scannedValue.value = result
                    val wifiInfo = parseWifiQRCode(result)
                    if (( wifiInfo != null) && ((wifiInfo.ssid.endsWith("AP") && selectedRole.value == "Boss")
                        ||(wifiInfo.ssid.endsWith("UP") && selectedRole.value == "Staff"))) {

                        increaseProgress(coroutineScope,progressState,0.5f,0.001f,100)
                        currentStep.value = NewDeviceStep.CONNECTING
                        connectToWifi(context, wifiInfo) {
                            val packet = if (selectedRole.value == "Boss") {
                                "${ssid.value}/${password.value}/${bossName.value}/${bossAssetId.value}"
                            } else {
                                "${ssid.value}/${password.value}/${bossAssetId.value}/${treeList[selectedTree].RecommendHumidity}"
                            }
                            increaseProgress(coroutineScope,progressState,0.9f,0.001f,100)

                            sendUdpPacket(packet,selectedRole.value == "Boss")
                            coroutineScope.launch{
                                addNotiToHashStore(context,
                                    NotiModel(bossName.value,"null",4,0)
                                )
                            }
                            increaseProgress(coroutineScope,progressState,0.99f,0.001f,50)
                            currentStep.value = NewDeviceStep.CHOOSE_ROLE
                        }
                    } else {
                        Toast.makeText(context, "QR không hợp lệ hoặc sai loại thiết bị", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        NewDeviceStep.CONNECTING -> {
            ProgressBarWithPercentage(progress = progressState.value)
        }
    }
}


fun sendUdpPacket(message: String,isBoss:Boolean) {
    Thread {
        try {
            val socket = DatagramSocket()
            val bossAddress = InetAddress.getByName("192.168.1.8")
            val staffAddress= InetAddress.getByName("192.168.4.1")
            val buf = message.toByteArray()
            val packet = DatagramPacket(buf, buf.size, if(isBoss)  bossAddress else staffAddress, 1234)
            socket.send(packet)
            Log.d("MainActivity", "sendUdpPacket: $buf ")
            //socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "sendUdpPacket: failed ")
        }
    }.start()
}



@Composable
fun ProgressBarWithPercentage(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center


    ) {

        Spacer(modifier = Modifier.height(80.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = NonChuoi,
            trackColor = XanhNavy,
            strokeCap = StrokeCap.Round,
            gapSize = 15.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = XanhNavy

        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đang thực hiện kết nối, vui lòng không tắt thiết bị",
            style = MaterialTheme.typography.titleMedium

        )

    }


}
fun increaseProgress(
    coroutineScope: CoroutineScope,
    progressState: MutableState<Float>,
    target: Float,
    step: Float = 0.05f,
    delayMillis: Long = 100
) {
    coroutineScope.launch {
        while (progressState.value < target) {
            progressState.value += step
            delay(delayMillis)
        }
    }
}





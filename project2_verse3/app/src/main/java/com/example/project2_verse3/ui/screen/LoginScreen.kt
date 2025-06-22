package com.example.project2_verse3.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project2_verse3.model.MainViewModel
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
import com.google.android.datatransport.BuildConfig
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: MainViewModel // Nhận ViewModel từ parent
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // **THEO DÕI CÁC TRẠNG THÁI**
    val isFetchingUserList by viewModel.isFetchingUserList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.user.collectAsState()
    val isDataFetched by viewModel.isDataFetched.collectAsState()
    val isFetchingData by viewModel.isFetchingData.collectAsState()

    // Load danh sách user ngay khi màn hình mở
    LaunchedEffect(Unit) {
        viewModel.loadUserList()
        if(navController.previousBackStackEntry==null){
            viewModel.logout()
            viewModel.resetDataState()
        }
    }

    // **THEO DÕI TRẠNG THÁI LOGIN VÀ FETCH DATA**
    LaunchedEffect(user, isFetchingData, isDataFetched) {
        if (user != null) {
            Log.d("LoginScreen", "User logged in: ${user!!.cusName}")
            if (isFetchingData) {
                Log.d("LoginScreen", "Data fetching in progress...")
            } else if (isDataFetched) {
                Log.d("LoginScreen", "Data fetch completed, ready for navigation")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng nhập", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // **HIỂN THỊ TRẠNG THÁI LOADING USER LIST**
        if (isFetchingUserList) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Đang tải danh sách tài khoản...")
        }
        // **HIỂN THỊ FORM LOGIN**
        else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                enabled = !isLoading && user == null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading && user == null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    message = null
                        viewModel.login(email, password) { result ->
                        message = result
                    }
                },
                enabled = !isLoading && user == null && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonColors(XanhNavy, NonChuoi, Color.White, Color.DarkGray)
            ) {
                when {
                    isLoading -> Text("Đang đăng nhập...")
                    user != null && isFetchingData -> Text("Đang tải dữ liệu...")
                    user != null && isDataFetched -> Text("Hoàn thành!")
                    else -> Text("Đăng nhập")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // **HIỂN THỊ THÔNG BÁO VÀ TRẠNG THÁI**
            message?.let {
                Text(
                    text = it,
                    color = if (it.contains("thành công")) Color.Green else Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // **HIỂN THỊ TRẠNG THÁI FETCH DATA SAU LOGIN**
            if (user != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chào mừng, ${user!!.cusName}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            isFetchingData -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đang tải dữ liệu...",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            isDataFetched -> {
                                Text(
                                    text = "✓ Dữ liệu đã sẵn sàng - Đang chuyển màn hình...",
                                    color = Color.Green,
                                    fontSize = 14.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = "Chuẩn bị tải dữ liệu...",
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (user != null && !isFetchingData && !isDataFetched) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.retryFetchData() }
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.getAppState(),
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
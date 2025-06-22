package com.example.project2_verse3

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project2_verse3.model.*
import com.example.project2_verse3.ui.navigation.*
import com.example.project2_verse3.ui.screen.BossListScreen
import com.example.project2_verse3.ui.screen.NewDeviceScreen
import com.example.project2_verse3.ui.screen.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2_verse3.ui.screen.LoginScreen
import fetchJwtToken

var greatToken: String = ""

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Fetch token ngay từ đầu như bạn đã làm
            LaunchedEffect(Unit) {
                try {
                    greatToken = fetchJwtToken().toString()
                    Log.d("MainActivity", "Token fetched successfully: ${greatToken.take(20)}...")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to fetch token: ${e.message}")
                }
            }

            val viewModel: MainViewModel = viewModel()
            MainScreen(viewModel)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // **THEO DÕI CÁC TRẠNG THÁI QUAN TRỌNG**
    val bossList by mainViewModel.bossListState.collectAsState()
    val user by mainViewModel.user.collectAsState()
    val isDataFetched by mainViewModel.isDataFetched.collectAsState()
    val isFetchingData by mainViewModel.isFetchingData.collectAsState()

    // **LOGIC ĐIỀU HƯỚNG DựA TRÊN TRẠNG THÁI**
    val shouldShowMainScreen = user != null && isDataFetched

    Log.d("MainActivity", "App State - User: ${user?.cusName}, DataFetched: $isDataFetched, FetchingData: $isFetchingData, BossList: ${bossList.size}")
    Log.d("MainActivity", "Current Route: $currentRoute")

    // **SỬ DỤNG MỘT NAVHOST DUY NHẤT CHO TẤT CẢ CÁC MÀN HÌNH**
    Scaffold(
        topBar = {
            if (shouldShowMainScreen && (currentRoute == "home" || currentRoute == "new_device")) {
                ScreenHeader(
                    title = if (currentRoute == "home") "Home" else "NewDevice"
                )
            }
        },
        bottomBar = {
            if (shouldShowMainScreen && (currentRoute == "home" ||
                        currentRoute == "new_device" ||
                        currentRoute == "profile")) {
                BottomBar(navController)
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "log_in",  // Bắt đầu từ login
            modifier = Modifier.padding(padding)
        ) {
            // **LOGIN SCREEN**
            composable("log_in") {
                LoginScreen(navController, mainViewModel)
            }

            // **DATA LOADING SCREEN**
            composable("data_loading") {
                DataLoadingScreen(
                    userName = user?.cusName ?: "",
                    onRetry = { mainViewModel.retryFetchData() }
                )
            }

            // **MAIN SCREENS**
            composable("profile") {
                ProfileScreen(mainUser, navController)
            }
            composable("home") {
                BossListScreen(bossList)
            }
            composable("new_device") {
                NewDeviceScreen()
            }
        }
    }

    // **NAVIGATION LOGIC DựA VÀO STATE CHANGES**
    LaunchedEffect(user, isDataFetched, isFetchingData) {
        Log.d("MainActivity", "Navigation LaunchedEffect triggered - User: ${user?.cusName}, DataFetched: $isDataFetched, FetchingData: $isFetchingData, CurrentRoute: $currentRoute")

        when {
            // User vừa login thành công, chuyển sang data loading
            user != null && !isDataFetched && currentRoute == "log_in" -> {
                Log.d("MainActivity", "Navigating to data_loading screen")
                navController.navigate("data_loading") {
                    popUpTo("log_in") { inclusive = true }
                }
            }

            // Data đã fetch xong, chuyển sang home
            user != null && isDataFetched && (currentRoute == "log_in" || currentRoute == "data_loading") -> {
                Log.d("MainActivity", "Navigating to home screen - data ready")
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true } // Clear toàn bộ back stack
                }
            }

            // User logout, quay về login
            user == null && currentRoute != "log_in" -> {
                Log.d("MainActivity", "Navigating back to login")
                navController.navigate("log_in") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}


@Composable
fun DataLoadingScreen(
    userName: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chào mừng, $userName!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Đang tải dữ liệu của bạn...",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vui lòng đợi trong giây lát",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
fun GeneralLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang khởi tạo ứng dụng...")
        }
    }
}
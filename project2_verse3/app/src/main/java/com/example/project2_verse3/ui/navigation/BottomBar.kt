package com.example.project2_verse3.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project2_verse3.R



sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Home : Screen("home", "Home", R.drawable.home_filled)
    object Profile : Screen("profile", "Profile", R.drawable.group_10)
    object NewDevice : Screen("new_device", "New Device", R.drawable.vector)
}

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Profile,
        Screen.NewDevice
    )

    // Đặt màu tùy chỉnh
    val backgroundColor = Color(0xFF86EE60)
    val selectedColor = Color.White
    val unselectedColor =  Color(0xFF2B3752)

    Box(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .background(Color.Transparent) // Không che phủ màn hình
    ) {
        Column {
            Box ( modifier = Modifier
                .height(3.dp)
                .fillMaxWidth())

            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)) // Bo góc trên
                    .background(backgroundColor), // Màu nền
                containerColor = backgroundColor
            ) {
                items.forEach { screen ->
                    val isSelected = navController.currentDestination?.route == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(screen.icon),
                                contentDescription = screen.title,
                                tint = if (isSelected) selectedColor else unselectedColor
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (isSelected) selectedColor else unselectedColor
                            )
                        },
                        selected = isSelected,
                        onClick = { navController.navigate(screen.route) }
                    )
                }
            }
        }
    }
}


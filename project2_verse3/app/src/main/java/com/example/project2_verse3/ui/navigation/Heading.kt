package com.example.project2_verse3.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project2_verse3.R
import com.example.project2_verse3.R.*
import com.example.project2_verse3.model.MainViewModel
import com.example.project2_verse3.model.UserModel
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy
//Tiêu đề trên màn hình Profile
@Composable
fun ScreenHeader(title: String) {
    if(title=="NewDevice"||title=="Home") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    Color(0xFF86EE60),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.BottomEnd)
                    .background(XanhNavy, shape = RoundedCornerShape(12.dp))

            ) {
                Text(
                    text = "12",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF86EE60),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = 30.dp)
            ) {

                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 4.dp)
                        .background(color = NonChuoi, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF4F7ED)
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp)
                        .background(Color(0xFF2B3752), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)

                ) {
                    Text(
                        text = "Hôm nay bạn đã tưới nước $title lần",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }else{
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Color(0xFF86EE60),
                )
        )
    }
}

@Composable
fun ProfileHeader(user: UserModel, navController: NavController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // cao hơn Header để chứa cả avatar
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Nothing",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 45.dp) // đẩy ra khỏi phần header

        )
        // Header (màu xanh bo góc)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    color = NonChuoi,
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            // Title ở giữa trên
            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
            IconButton(
                onClick ={
                    expanded = true
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(45.dp)
            ) {
                Icon(
                    imageVector =Icons.Default.Lock,
                    contentDescription = "Login",
                    tint = Begie
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = XanhNavy,
            ) {
                DropdownMenuItem(
                    text = { Text("Setting", color = NonChuoi) },
                    onClick = {
                        expanded = false
                      Toast.makeText(context,"Hiện chức năng này chưa phát hành , hãy chờ đợi nhé",Toast.LENGTH_SHORT).show()
                    //    navController.navigate("setting")

                    }
                )
                DropdownMenuItem(
                    text = { Text("Log out", color = NonChuoi) },
                    onClick = {
                        expanded = false
                        navController.navigate("log_in"){
                            viewModel.resetDataState()
                            viewModel.logout()
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

        }
        Image(
            painter = painterResource(id = user.cusImage),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(175.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 35.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 3.dp, color = Begie , shape = CircleShape )

        )
        Text(
            text = user.cusName,
            color = Color.Black,
            fontSize = 35.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 10.dp).align(Alignment.BottomCenter).offset(y=90.dp)
        )

    }
}




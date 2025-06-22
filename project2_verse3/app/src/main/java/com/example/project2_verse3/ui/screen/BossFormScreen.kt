package com.example.project2_verse3.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy

@Composable
fun BossFormScreen(onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var bossName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Thiết lập Boss:")
        OutlinedTextField(value = ssid, onValueChange = { ssid = it }, label = { Text("SSID") } )
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        OutlinedTextField(value = bossName, onValueChange = { bossName = it }, label = { Text("Boss Name") })
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = onBack,
                modifier = Modifier.weight(1f),
                colors= ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi))
                { Text("Quay lại") }

            Spacer(Modifier.width(8.dp))

            Button(onClick = { onSend(ssid, password, bossName) },
                modifier = Modifier.weight(1f),
                colors= ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi)) { Text("Gửi") }
        }
    }
}

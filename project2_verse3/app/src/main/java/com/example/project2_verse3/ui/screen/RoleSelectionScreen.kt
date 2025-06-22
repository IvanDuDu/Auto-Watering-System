package com.example.project2_verse3.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy

@Composable
fun RoleSelectionScreen(selectedRole: String, onRoleSelected: (String) -> Unit, onNext: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Chọn loại thiết bị:")
        Row {
            listOf("Boss", "Staff").forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    RadioButton(selected = role == selectedRole,
                        onClick = { onRoleSelected(role) },
                        colors = RadioButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi))
                    Spacer(Modifier.width(8.dp))
                    Text(role)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, colors = ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi)) { Text("Tiếp tục") }
    }
}

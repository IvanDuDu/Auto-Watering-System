package com.example.project2_verse3.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2_verse3.model.BossModel
import com.example.project2_verse3.model.MainViewModel
import com.example.project2_verse3.model.treeList
import com.example.project2_verse3.ui.theme.Begie
import com.example.project2_verse3.ui.theme.NonChuoi
import com.example.project2_verse3.ui.theme.XanhNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormScreen(
    bossList: List<BossModel>,
    selectedBossId: String,
    selectedTree: Int,
    onBossSelected: (String) -> Unit,
    onTreeSelected: (Int) -> Unit,
    onBack: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var bossMenuExpanded by remember { mutableStateOf(false) }
    var treeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Thiết lập Staff", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Nhập Wi-Fi
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("Wi-Fi SSID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wi-Fi Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn Boss (Dropdown)
        Text("Chọn Boss:", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = bossMenuExpanded,
            onExpandedChange = { bossMenuExpanded = !bossMenuExpanded }
        ) {
            OutlinedTextField(
                value = bossList.find { it.BossID == selectedBossId }?.bossName ?: "Chọn Boss",
                onValueChange = {},
                readOnly = true,
                label = { Text("Boss") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bossMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()

            )

            DropdownMenu(
                expanded = bossMenuExpanded,
                onDismissRequest = { bossMenuExpanded = false },
                containerColor = XanhNavy

            ) {
                bossList.forEach { boss ->
                    DropdownMenuItem(
                        text = { Text(boss.bossName, color = NonChuoi) },
                        onClick = {
                            onBossSelected(boss.BossID)
                            ssid = boss.wifiInfo.ssid
                            password = boss.wifiInfo.password
                            bossMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn Cây (Dropdown)
        Text("Chọn Cây:", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = treeMenuExpanded,
            onExpandedChange = { treeMenuExpanded = !treeMenuExpanded }
        ) {
            OutlinedTextField(
                value = treeList.getOrNull(selectedTree)?.name ?: "Chọn cây",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cây") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = treeMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            DropdownMenu(
                expanded = treeMenuExpanded,
                onDismissRequest = { treeMenuExpanded = false },
                containerColor = XanhNavy
            ) {
                treeList.forEachIndexed { index, tree ->
                    DropdownMenuItem(
                        text = { Text(tree.name , color = NonChuoi) },
                        onClick = {
                            onTreeSelected(index)
                            treeMenuExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Nút điều hướng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors= ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi)

            ) {
                Text("Quay lại")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onSend(ssid, password) },
                modifier = Modifier.weight(1f),
                colors= ButtonColors(XanhNavy, NonChuoi, Begie, NonChuoi)
            ) {
                Text("Gửi")
            }
        }
    }
}


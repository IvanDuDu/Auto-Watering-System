package com.example.project2_verse3.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TimeDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nhập thông tin") },
        text = {
            Column {
                Text(text = "Vui lòng nhập một số:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        // Chỉ cho phép nhập số
                        if (newValue.all { it.isDigit() }) {
                            textValue = newValue
                        }
                    },
                    placeholder = { Text("Nhập số") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textValue.isNotEmpty()) {
                        onSubmit(textValue.toInt())
                    } }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Huỷ")
            }
        }
    )
}

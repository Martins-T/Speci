package lv.it20071.speci.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Mainīt paroli") },
        text = {
            Column {
                Text(text = "Ievadiet jauno paroli:")
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Jaunā parole") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newPassword.isNotEmpty()) {
                    onChangePassword(newPassword)
                    onDismiss()
                }
            }) {
                Text("Saglabāt")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Atcelt")
            }
        }
    )
}

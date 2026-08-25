package com.remebrit.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.remebrit.data.db.RemebritDatabase
import com.remebrit.data.repository.ItemRepository
import kotlinx.coroutines.launch
import com.remebrit.ui.theme.RemebritTheme

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: ""

        val repository = ItemRepository(RemebritDatabase.getInstance(applicationContext).itemDao())

        setContent {
            RemebritTheme {
                Surface {
                    ShareCaptureScreen(
                        initialText = sharedText,
                        onSave = { finalText ->
                            lifecycleScope.launch {
                                repository.capture(finalText)
                                finish()
                            }
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareCaptureScreen(initialText: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(initialText) }

    Column(modifier = Modifier.padding(20.dp)) {
        Text("Save this?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (text.isNotBlank()) onSave(text) }) { Text("Save") }
        }
    }
}
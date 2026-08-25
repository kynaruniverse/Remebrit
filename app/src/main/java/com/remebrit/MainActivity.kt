package com.remebrit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.remebrit.data.db.RemebritDatabase
import com.remebrit.data.repository.ItemRepository
import com.remebrit.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ItemRepository(RemebritDatabase.getInstance(applicationContext).itemDao())
        setContent { RemebritApp(repository) }
    }
}

@Composable
fun RemebritApp(repository: ItemRepository) {
    val factory = viewModelFactory {
        initializer { HomeViewModel(repository) }
    }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val items by viewModel.items.collectAsState()
    var text by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text("Remebrit", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("What do you need to remember?") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            if (text.isNotBlank()) {
                                viewModel.capture(text)
                                text = ""
                            }
                        }) { Text("Save") }
                    }
                )

                Spacer(Modifier.height(24.dp))
                Text("INBOX", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = false, onCheckedChange = { viewModel.complete(item) })
                            Text(item.content)
                        }
                    }
                }
            }
        }
    }
}
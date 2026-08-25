package com.remebrit.ui.search

import androidx.compose.foundation.clickable
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
import com.remebrit.data.repository.ItemRepository

@Composable
fun SearchScreen(repository: ItemRepository, onBack: () -> Unit, onOpenItem: (Long) -> Unit) {
    val factory = viewModelFactory { initializer { SearchViewModel(repository) } }
    val viewModel: SearchViewModel = viewModel(factory = factory)
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("Search Remebrit...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (query.isNotBlank() && results.isEmpty()) {
            Text("Nothing found.")
        }

        LazyColumn {
            items(results) { item ->
                Text(
                    item.content,
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable { onOpenItem(item.id) }
                )
            }
        }
    }
}
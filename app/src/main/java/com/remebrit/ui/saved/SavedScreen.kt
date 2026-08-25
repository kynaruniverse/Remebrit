package com.remebrit.ui.saved

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.remebrit.ItemRow
import com.remebrit.data.repository.ItemRepository

@Composable
fun SavedScreen(repository: ItemRepository, onOpenItem: (Long) -> Unit) {
    val factory = viewModelFactory { initializer { SavedViewModel(repository) } }
    val viewModel: SavedViewModel = viewModel(factory = factory)
    val items by viewModel.savedItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Saved", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (items.isEmpty()) Text("Nothing kept yet.")
        LazyColumn {
            items(items) { item -> ItemRow(item, onOpenItem) }
        }
    }
}
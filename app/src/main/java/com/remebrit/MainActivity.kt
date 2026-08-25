package com.remebrit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remebrit.data.db.RemebritDatabase
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import com.remebrit.ui.home.HomeViewModel
import com.remebrit.ui.item.ItemDetailViewModel
import com.remebrit.ui.search.SearchScreen
import com.remebrit.ui.theme.RemebritTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ItemRepository(RemebritDatabase.getInstance(applicationContext).itemDao())
        setContent { RemebritApp(repository) }
    }
}

@Composable
fun RemebritApp(repository: ItemRepository) {
    val navController = rememberNavController()
    RemebritTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        repository,
                        onOpenItem = { id -> navController.navigate("item/$id") },
                        onSearch = { navController.navigate("search") }
                    )
                }
                composable("search") {
                    SearchScreen(repository, onBack = { navController.popBackStack() }) { id ->
                        navController.navigate("item/$id")
                    }
                }
                composable("item/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                    if (id != null) {
                        ItemDetailScreen(repository, id) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(repository: ItemRepository, onOpenItem: (Long) -> Unit, onSearch: () -> Unit) {
    val factory = viewModelFactory { initializer { HomeViewModel(repository) } }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val sections by viewModel.sections.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Remebrit", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onSearch) { Text("Search") }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("What do you need to remember?") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (text.isNotBlank()) { viewModel.capture(text); text = "" }
            }),
            trailingIcon = {
                TextButton(onClick = {
                    if (text.isNotBlank()) { viewModel.capture(text); text = "" }
                }) { Text("Save") }
            }
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn {
            if (sections.now.isNotEmpty()) {
                item { SectionHeader("NOW") }
                items(sections.now) { ItemRow(it, onOpenItem) }
            }
            if (sections.today.isNotEmpty()) {
                item { SectionHeader("TODAY") }
                items(sections.today) { ItemRow(it, onOpenItem) }
            }
            item { SectionHeader("INBOX") }
            items(sections.inbox) { ItemRow(it, onOpenItem) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun ItemRow(item: RemebritItem, onOpenItem: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).clickable { onOpenItem(item.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.content)
    }
}

@Composable
fun ItemDetailScreen(repository: ItemRepository, itemId: Long, onBack: () -> Unit) {
    val factory = viewModelFactory { initializer { ItemDetailViewModel(repository, itemId) } }
    val viewModel: ItemDetailViewModel = viewModel(factory = factory)
    val item by viewModel.item.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Spacer(Modifier.height(16.dp))

        item?.let { current ->
            Text(current.content, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Status: ${current.status}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.complete(current); onBack() }) { Text("Complete") }
                OutlinedButton(onClick = { viewModel.snoozeUntilTomorrow(current); onBack() }) { Text("Snooze") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.keep(current); onBack() }) { Text("Keep") }
                OutlinedButton(onClick = { viewModel.archive(current); onBack() }) { Text("Archive") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.delete(current); onBack() }) { Text("Delete") }
        } ?: Text("Loading…")
    }
}
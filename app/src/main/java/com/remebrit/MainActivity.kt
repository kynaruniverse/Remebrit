package com.remebrit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remebrit.data.db.RemebritDatabase
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import com.remebrit.domain.parsing.CaptureParser
import com.remebrit.domain.parsing.EntityType
import com.remebrit.ui.home.HomeViewModel
import com.remebrit.ui.item.ItemDetailViewModel
import com.remebrit.ui.saved.SavedScreen
import com.remebrit.ui.search.SearchScreen
import com.remebrit.ui.theme.RemebritTheme
import com.remebrit.ui.theme.ThemeMode
import com.remebrit.ui.theme.ThemePreferences
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ItemRepository(RemebritDatabase.getInstance(applicationContext).itemDao())
        setContent { RemebritApp(repository) }
    }
}

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination("home", "Home", Icons.Filled.Home),
    BottomDestination("search", "Search", Icons.Filled.Search),
    BottomDestination("saved", "Saved", Icons.Filled.Star)
)

@Composable
fun RemebritApp(repository: ItemRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(ThemePreferences.get(context)) }

    RemebritTheme(themeMode = themeMode) {
        Scaffold(
            bottomBar = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                if (bottomDestinations.any { it.route == currentRoute }) {
                    NavigationBar {
                        bottomDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = destination.label) },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            repository,
                            onOpenItem = { id -> navController.navigate("item/$id") },
                            themeMode = themeMode,
                            onToggleTheme = {
                                themeMode = when (themeMode) {
                                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                    ThemeMode.LIGHT -> ThemeMode.DARK
                                    ThemeMode.DARK -> ThemeMode.SYSTEM
                                }
                                ThemePreferences.set(context, themeMode)
                            }
                        )
                    }
                    composable("search") {
                        SearchScreen(repository, onBack = { navController.popBackStack() }) { id ->
                            navController.navigate("item/$id")
                        }
                    }
                    composable("saved") {
                        SavedScreen(repository) { id -> navController.navigate("item/$id") }
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
}

@Composable
fun LogoMark() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "R",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun HomeScreen(
    repository: ItemRepository,
    onOpenItem: (Long) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    val factory = viewModelFactory { initializer { HomeViewModel(repository) } }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val sections by viewModel.sections.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoMark()
                    Spacer(Modifier.width(10.dp))
                    Text("Remebrit", style = MaterialTheme.typography.headlineMedium)
                }
                TextButton(onClick = onToggleTheme) {
                    Text(when (themeMode) {
                        ThemeMode.SYSTEM -> "Auto"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("What do you need to remember?") },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = MaterialTheme.shapes.small, clip = false),
            shape = MaterialTheme.shapes.small,
            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
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

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (sections.now.isNotEmpty()) {
                item { SectionHeader("NOW", sections.now.size) }
                items(sections.now) { ItemRow(it, onOpenItem) }
                item { HorizontalDivider(modifier = Modifier.padding(top = 12.dp)) }
            }
            if (sections.today.isNotEmpty()) {
                item { SectionHeader("TODAY", sections.today.size) }
                items(sections.today) { ItemRow(it, onOpenItem) }
                item { HorizontalDivider(modifier = Modifier.padding(top = 12.dp)) }
            }
            item { SectionHeader("INBOX", sections.inbox.size) }
            items(sections.inbox) { ItemRow(it, onOpenItem) }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "$title ($count)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ItemRow(item: RemebritItem, onOpenItem: (Long) -> Unit) {
    val typeIcon = remember(item.content) { iconForContent(item.content) }
    val dateLabel = remember(item.relevantAt) { relativeDateLabel(item.relevantAt) }

    OutlinedCard(
        onClick = { onOpenItem(item.id) },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                typeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(item.content, modifier = Modifier.weight(1f))
            dateLabel?.let {
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text(it, style = MaterialTheme.typography.labelSmall) })
            }
        }
    }
}

private fun iconForContent(content: String): ImageVector {
    val entities = CaptureParser.parse(content)
    return when {
        entities.any { it.type == EntityType.URL } -> Icons.Filled.Link
        entities.any { it.type == EntityType.MONEY } -> Icons.Filled.AttachMoney
        entities.any { it.type == EntityType.DATE || it.type == EntityType.TIME } -> Icons.Filled.Schedule
        entities.any { it.type == EntityType.TASK } -> Icons.Filled.CheckCircle
        else -> Icons.Filled.Notes
    }
}

private fun relativeDateLabel(relevantAtMillis: Long?, zone: ZoneId = ZoneId.systemDefault()): String? {
    if (relevantAtMillis == null) return null
    val relevantDate = Instant.ofEpochMilli(relevantAtMillis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when {
        relevantDate.isEqual(today) -> "Today"
        relevantDate.isEqual(today.plusDays(1)) -> "Tomorrow"
        else -> relevantDate.format(DateTimeFormatter.ofPattern("d MMM"))
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
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    current.status.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                DetailAction(Icons.Filled.CheckCircle, "Complete") { viewModel.complete(current); onBack() }
                DetailAction(Icons.Filled.Snooze, "Snooze") { viewModel.snoozeUntilTomorrow(current); onBack() }
                DetailAction(Icons.Filled.Star, "Keep") { viewModel.keep(current); onBack() }
                DetailAction(Icons.Filled.Archive, "Archive") { viewModel.archive(current); onBack() }
                DetailAction(Icons.Filled.DeleteOutline, "Delete") { viewModel.delete(current); onBack() }
            }
        } ?: Text("Loading…")
    }
}

@Composable
private fun DetailAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.remebrit.ui.home.HomeViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.remebrit.notifications.ReminderScheduler
import com.remebrit.ui.item.ItemDetailViewModel
import com.remebrit.ui.saved.SavedScreen
import com.remebrit.ui.search.SearchScreen
import com.remebrit.onboarding.OnboardingPreferences
import com.remebrit.ui.onboarding.OnboardingScreen
import com.remebrit.ui.settings.SettingsScreen
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
    val startDestination = remember { if (OnboardingPreferences.isCompleted(context)) "home" else "onboarding" }

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
                                label = { Text(destination.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("onboarding") {
                        OnboardingScreen(onFinished = {
                            OnboardingPreferences.setCompleted(context)
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        })
                    }
                    composable("home") {
                        HomeScreen(
                            repository,
                            onOpenItem = { id -> navController.navigate("item/$id") },
                            themeMode = themeMode,
                            onOpenSettings = { navController.navigate("settings") },
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
                    composable("settings") {
                        SettingsScreen(repository) { navController.popBackStack() }
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
fun HomeScreen(
    repository: ItemRepository,
    onOpenItem: (Long) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val factory = viewModelFactory { initializer { HomeViewModel(repository) } }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val sections by viewModel.sections.collectAsState()
    var text by remember { mutableStateOf("") }
    val allItems = sections.now + sections.today + sections.inbox

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Remebrit",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                TextButton(onClick = onToggleTheme) {
                    Text(when (themeMode) {
                        ThemeMode.SYSTEM -> "Auto"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    })
                }
                TextButton(onClick = onOpenSettings) { Text("Settings") }
            }
        }
        Spacer(Modifier.height(20.dp))

        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("What do you need to remember?") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
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

        Spacer(Modifier.height(28.dp))

        LazyColumn {
            items(allItems) { ItemRow(it, onOpenItem) }
        }
    }
}

@Composable
fun ItemRow(item: RemebritItem, onOpenItem: (Long) -> Unit) {
    val dateLabel = remember(item.relevantAt) { relativeDateLabel(item.relevantAt) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenItem(item.id) }
            .padding(vertical = 14.dp)
    ) {
        Text(item.content, style = MaterialTheme.typography.bodyLarge)
        dateLabel?.let {
            Spacer(Modifier.height(2.dp))
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    val plain = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    var reminderStatus by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val current = item
        if (granted && current?.relevantAt != null) {
            ReminderScheduler.schedule(context, current.id, current.content, current.relevantAt!!)
            reminderStatus = "Reminder set"
        } else {
            reminderStatus = "Notifications permission not granted"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(contentColor = plain)
        ) { Text("← Back") }
        Spacer(Modifier.height(16.dp))

        item?.let { current ->
            Text(current.content, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                current.status.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            val actionColors = ButtonDefaults.textButtonColors(contentColor = plain)

            if (current.relevantAt != null && current.relevantAt > System.currentTimeMillis()) {
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            ReminderScheduler.schedule(context, current.id, current.content, current.relevantAt!!)
                            reminderStatus = "Reminder set"
                        }
                    },
                    colors = actionColors
                ) { Text("Remind me") }
                reminderStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { viewModel.complete(current); onBack() }, colors = actionColors) { Text("Complete") }
                TextButton(onClick = { viewModel.snoozeUntilTomorrow(current); onBack() }, colors = actionColors) { Text("Snooze") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { viewModel.keep(current); onBack() }, colors = actionColors) { Text("Keep") }
                TextButton(onClick = { viewModel.archive(current); onBack() }, colors = actionColors) { Text("Archive") }
            }
            TextButton(onClick = { viewModel.delete(current); onBack() }, colors = actionColors) { Text("Delete") }
        } ?: Text("Loading…")
    }
}
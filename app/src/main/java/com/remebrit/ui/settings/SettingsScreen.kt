package com.remebrit.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.remebrit.data.entity.ItemStatus
import com.remebrit.data.entity.RemebritItem
import com.remebrit.data.repository.ItemRepository
import com.remebrit.notifications.NotificationPreferences
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SettingsScreen(repository: ItemRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(NotificationPreferences.isEnabled(context)) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val items = repository.exportAll()
            val json = JSONArray()
            items.forEach { item ->
                json.put(JSONObject().apply {
                    put("content", item.content)
                    put("createdAt", item.createdAt)
                    put("status", item.status.name)
                    put("relevantAt", item.relevantAt ?: JSONObject.NULL)
                    put("completedAt", item.completedAt ?: JSONObject.NULL)
                })
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toString(2).toByteArray())
            }
            statusMessage = "Exported ${items.size} items"
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) {
                val array = JSONArray(text)
                val imported = mutableListOf<RemebritItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    imported += RemebritItem(
                        content = obj.getString("content"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        status = runCatching { ItemStatus.valueOf(obj.getString("status")) }.getOrDefault(ItemStatus.ACTIVE),
                        relevantAt = if (obj.isNull("relevantAt")) null else obj.getLong("relevantAt"),
                        completedAt = if (obj.isNull("completedAt")) null else obj.getLong("completedAt")
                    )
                }
                repository.importAll(imported)
                statusMessage = "Imported ${imported.size} items"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Spacer(Modifier.height(16.dp))
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Notifications", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Allow reminders")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    NotificationPreferences.setEnabled(context, it)
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("Privacy & Security", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Your data stays on your device. Remebrit doesn't require an account. Core features work offline.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))
        Text("Backup & Export", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { exportLauncher.launch("remebrit-backup.json") }) { Text("Export") }
            TextButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) { Text("Import") }
        }
        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))
        Text("About Remebrit", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "A personal memory inbox. No ads, no tracking, no account required.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
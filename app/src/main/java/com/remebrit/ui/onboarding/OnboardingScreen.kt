package com.remebrit.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class OnboardingPage(val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(
        "Meet Remebrit",
        "The place for everything you need to remember, but don't want to organise."
    ),
    OnboardingPage(
        "Don't organise it.",
        "Just put it here. \"Buy milk.\" \"Parking C17.\" \"Call Mum Friday.\" \"That jacket I liked.\""
    ),
    OnboardingPage(
        "Remebrit remembers the details.",
        "It can recognise dates, times, links and useful information automatically."
    ),
    OnboardingPage(
        "Private by default.",
        "Your Remebrit data stays on your device."
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    val page = pages[index]
    val isLast = index == pages.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(page.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(page.body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLast) {
                TextButton(onClick = onFinished) { Text("Skip") }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            TextButton(onClick = {
                if (isLast) onFinished() else index++
            }) {
                Text(if (isLast) "Start Remebriting" else "Continue")
            }
        }
    }
}
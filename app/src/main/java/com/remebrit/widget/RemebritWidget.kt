package com.remebrit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.remebrit.MainActivity

class RemebritWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF9F6))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Text(
                    "REMEBRIT",
                    style = TextStyle(color = ColorProvider(Color(0xFFE7A83B)), fontWeight = FontWeight.Bold)
                )
                Text(
                    "What's on your mind?",
                    style = TextStyle(color = ColorProvider(Color(0xFF252522)))
                )
            }
        }
    }
}
package com.captain.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.captain.control.BotCaptainEvents

class AppComponents {
    @Preview
    @Composable
    fun AppHeader () {
        Column (
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Bot Captain", style = MaterialTheme.typography.headlineLarge)
        }
    }

    @Composable
    fun AppFooter (eventHandler: BotCaptainEvents) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        )
        {
            var fontWeight = FontWeight.Bold
            var buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.inversePrimary)

            Button(content={ Text("Poweroff Network", fontWeight = fontWeight) }, colors= buttonColors, border = null, onClick = { eventHandler.shutdownMobileWifi() }, enabled = false)
        }
    }
}
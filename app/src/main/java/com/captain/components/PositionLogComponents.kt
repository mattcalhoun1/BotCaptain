package com.captain.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState
import kotlin.math.roundToInt

class PositionLogComponents {
    @Composable
    fun LogContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        val headerText =  remember { mutableStateOf(if (botCaptainState.newestLogEntry.equals(""))  "No Logs Yet" else botCaptainState.newestLogEntry) }
        eventHandler.setPositionLogListener(headerText)

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            // this will recompose when [header] changes, but not when [names] changes
            Text (text = headerText.value, style = MaterialTheme.typography.titleMedium)
            Button(onClick = { eventHandler.refreshSession() }) {
                Text(text = "Refresh")
            }
            Divider()

            LazyColumn {
                items(
                    items = botCaptainState.positionLog,
                    key = { pl ->
                        // Return a stable + unique key for the item
                        pl.entryNum
                    }
                ) { pl ->
                    val niceOccurred = pl.occurred.substring(pl.occurred.indexOf('T') + 1, pl.occurred.indexOf('Z'))
                    val roundedX = ((pl.positionX)*100).roundToInt() / 100.0
                    val roundedY = ((pl.positionY)*100).roundToInt() / 100.0
                    val roundedHeading = ((pl.heading)*100).roundToInt() / 100.0
                    Text("${niceOccurred} : ( ${roundedX} , ${roundedY} ) > ${roundedHeading} deg")
                }
            }
        }
    }

}
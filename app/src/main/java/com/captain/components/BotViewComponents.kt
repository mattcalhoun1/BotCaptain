package com.captain.components

import android.graphics.ImageDecoder
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.captain.services.navsvc.SearchHitImage
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState
import com.captain.services.navsvc.PositionLogEntry
import kotlin.math.roundToInt

class BotViewComponents {
    fun findPositionLogEntry(botCaptainState: BotCaptainState, entryNum: Long) : PositionLogEntry {
        for (p in botCaptainState.positionLog) {
            if (p.entryNum == entryNum) {
                return p
            }
        }

        return botCaptainState.positionLog[0]
    }

    @Composable
    fun PositionViewContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        val headerText =  remember { mutableStateOf(if (botCaptainState.newestLogEntry.equals(""))  "No Images Yet" else botCaptainState.newestLogEntry) }
        eventHandler.setPositionImageListener(headerText)

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // this will recompose when [header] changes, but not when [names] changes
            Text (text = headerText.value, style = MaterialTheme.typography.titleMedium)
            Button(onClick = { eventHandler.refreshSession() }) {
                Text(text = "Refresh")
            }
            Divider()

            LazyColumn {
                items(
                    items = botCaptainState.positionImages,
                    key = { img ->
                        // Return a stable + unique key for the item
                        "${img::class.simpleName}.${img.entryNum}.${img.cameraId}.${img.cameraAngle}"
                    }
                ) { img ->
                    val decodedHardcoded = Base64.decode(img.encodedImage,
                        Base64.DEFAULT)
                    val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(decodedHardcoded)).asImageBitmap()
                    val entry  = findPositionLogEntry(botCaptainState = botCaptainState, entryNum = img.entryNum)

                    var label : String = "(${entry.positionX.roundToInt()},${entry.positionY.roundToInt()}) ${entry.heading.roundToInt()}º (Cam: ${img.cameraId}, ${img.cameraAngle})"
                    if (img is SearchHitImage) {
                        label = "Found ${(img as SearchHitImage).objectType}"
                    }
                    Text(label, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    Image(bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.size(Dp(400.0F))
                    )
                    Divider()

                }
            }
        }
    }

}
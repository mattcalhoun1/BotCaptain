package com.captain.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.captain.services.navsvc.BotSession
import com.captain.services.navsvc.Vehicle
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState

class VehicleSelectionComponents {
    @Composable
    fun SelectionsContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                VehiclePicker(
                    header = "Select One",
                    vehicles = botCaptainState.vehicles,
                    eventHandler = eventHandler,
                    botCaptainState = botCaptainState
                )
            }
            Box {
                SessionPicker(
                    header = "Select One",
                    sessions = botCaptainState.botSessions,
                    eventHandler = eventHandler,
                    botCaptainState = botCaptainState
                )
            }
        }
    }

    @Composable
    private fun VehiclePicker(
        header: String,
        vehicles: List<Vehicle>,
        eventHandler: BotCaptainEvents,
        botCaptainState: BotCaptainState
    ) {
        // Changing the header text triggers a recompose, so we hand
        // that to the event handler so it can trigger as needed
        val headerText =  remember { mutableStateOf(if (botCaptainState.selectedVehicle.equals(""))  header else botCaptainState.selectedVehicle) }
        eventHandler.setVehicleSelectionListener(headerText)

        Log.i("picker", "Recomposing vehicle picker")

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // this will recompose when [header] changes, but not when [names] changes
            Text(text = "Vehicle: " + headerText.value, style = MaterialTheme.typography.titleMedium)
            Divider()

            // LazyColumn is the Compose version of a RecyclerView.
            // The lambda passed to items() is similar to a RecyclerView.ViewHolder.
            LazyRow {
                items(vehicles) { v ->
                    // When an item's [name] updates, the adapter for that item
                    // will recompose. This will not recompose when [header] changes
                    VehiclePickerItem(v.vehicleId, eventHandler)
                }
            }
        }
    }

    /**
     * Display a single name the user can click.
     */
    @Composable
    private fun VehiclePickerItem(vehicleId: String, eventHandler: BotCaptainEvents) {
        // if this vehicle is selected, highlight it
        var fontWeight = FontWeight.Normal
        var buttonColors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary)


        if (eventHandler.isVehicleSelected(vehicleId)) {
            fontWeight = FontWeight.Bold
            buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.inversePrimary)
        }

        Button(content={ Text(vehicleId, fontWeight = fontWeight) }, colors= buttonColors, border = null, onClick = { eventHandler.vehicleSelected(vehicleId) })
        //Text(vehicleId,  style = MaterialTheme.typography.displaySmall, fontWeight=fontWeight, modifier= Modifier.clickable(onClick = { eventHandler.vehicleSelected(vehicleId) }))
    }

    @Composable
    private fun SessionPicker(
        header: String,
        sessions: List<BotSession>,
        eventHandler: BotCaptainEvents,
        botCaptainState: BotCaptainState
    ) {
        val headerText =  remember { mutableStateOf(if (botCaptainState.selectedSession.equals(""))  header else botCaptainState.selectedSession) }
        eventHandler.setSessionSelectionListener(headerText)

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // this will recompose when [header] changes, but not when [names] changes
            Text("Session: " + headerText.value, style = MaterialTheme.typography.titleMedium)
            Divider()

            // LazyColumn is the Compose version of a RecyclerView.
            // The lambda passed to items() is similar to a RecyclerView.ViewHolder.
            LazyRow {
                items(sessions) { s ->
                    // When an item's [name] updates, the adapter for that item
                    // will recompose. This will not recompose when [header] changes
                    NavSessionPickerItem(s.sessionId, eventHandler)
                }
            }
        }
    }

    /**
     * Display a single name the user can click.
     */
    @Composable
    private fun NavSessionPickerItem(sessionId: String, eventHandler: BotCaptainEvents) {
        var fontWeight = FontWeight.Normal
        var buttonColors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary)


        if (eventHandler.isSessionSelected(sessionId)) {
            fontWeight = FontWeight.Bold
            buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.inversePrimary)
        }

        Button(content={ Text(sessionId, fontWeight = fontWeight) }, colors= buttonColors, border = null, onClick = { eventHandler.sessionSelected(sessionId) })

    }

}
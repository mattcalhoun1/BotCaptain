package com.captain.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.captain.services.navsvc.AssignmentDetails
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState
import kotlin.math.roundToInt

class AssignmentComponents {
    var inputX : Int = 0
    var inputY : Int = 0

    @Composable
    fun AssignmentsContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        Assignments(
            eventHandler = eventHandler,
            botCaptainState = botCaptainState
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Assignments(
        eventHandler: BotCaptainEvents,
        botCaptainState: BotCaptainState
    ) {
        // Changing the header text triggers a recompose, so we hand
        // that to the event handler so it can trigger as needed
        val headerText =  remember { mutableStateOf("${botCaptainState.openAssignments.size}") }
        eventHandler.setAssignmentRefreshListener(headerText)

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (botCaptainState.selectedVehicle.equals("")) {
                Text(
                    text = "${botCaptainState.selectedVehicle} - Open Assignments: ${headerText.value}",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                // this hack triggers the assignment selection ui update. there must be a better way
                if (!botCaptainState.assignmentMap.equals("")) {
                    eventHandler.assignmentMapSelected(botCaptainState.assignmentMap)
                }

                // this will recompose when [header] changes, but not when [names] changes
                Text(
                    text = "${botCaptainState.selectedVehicle} - Open Assignments: ${headerText.value}",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(thickness = Dp(3.0F), color = Color.Transparent)
                MapSelector("Selected Map", eventHandler = eventHandler, botCaptainState = botCaptainState)

                SectionDivider ()

                Text(text = "Movement")
                Row {
                    DriveModePickerItem(eventHandler)
                    ButtonSpacer()
                    Button(onClick = { eventHandler.sendAssignmentGo(x = inputX.toFloat(), y = inputY.toFloat()) }) {
                        Text("Go")
                    }
                    ButtonSpacer()
                    Button(onClick = { eventHandler.sendAssignmentFace(x = inputX.toFloat(), y = inputY.toFloat()) }) {
                        Text("Face")
                    }
                }
                Row {
                    CoordXPicker(mapId = botCaptainState.assignmentMap, eventHandler = eventHandler, navState = botCaptainState)
                    ButtonSpacer()
                    CoordYPicker(mapId = botCaptainState.assignmentMap, eventHandler = eventHandler, navState = botCaptainState)
                }

                SectionDivider ()
                Text(text = "Environment")
                Row{
                    Button(onClick = { eventHandler.sendAssignmentGetPosition() }) {
                        Text("Log Coords")
                    }
                    ButtonSpacer()
                    Button(onClick = { eventHandler.sendAssignmentLogLidar() }) {
                        Text("Log Lidar")
                    }
                    ButtonSpacer()
                    SearchPickerItem(mapId = botCaptainState.assignmentMap, eventHandler = eventHandler, navState = botCaptainState)
                }

                SectionDivider ()
                Text(text = "Maintenance")
                Row {
                    Button(onClick = { eventHandler.clearAssignments() }) {
                        Text("Clear Assignments")
                    }
                    ButtonSpacer()
                    Button(onClick = { eventHandler.sendAssignmentShutdown() }) {
                        Text("Shut Down")
                    }
                }

                SectionDivider ()

                Text(text = "Open Assignments", style = MaterialTheme.typography.titleMedium)
                Divider(thickness = Dp(3.0F))
                // Show current assignments
                LazyColumn {
                    items(botCaptainState.openAssignments) { a ->
                        // When an item's [name] updates, the adapter for that item
                        // will recompose. This will not recompose when [header] changes
                        AssignmentItem(assignment = a, eventHandler = eventHandler)
                        Divider()
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionDivider () {
        Divider(thickness = Dp(7.0F), color = Color.Transparent)
        Divider(thickness = Dp(1.0F), color = Color.DarkGray)
        Divider(thickness = Dp(7.0F), color = Color.Transparent)
    }

    private fun updateX(userInput : String) : Int {
        Log.i("test", "new X: ${userInput}")
        try {
            inputX = userInput.toInt()
        } catch (e : Exception) {
            // ignore the input
        }
        return inputX
    }

    private fun updateY(userInput : String) : Int {
        Log.i("test", "new Y: ${userInput}")
        try {
            inputY = userInput.toInt()
        } catch (e : Exception) {
            // ignore the input
        }
        return inputY
    }

    @Composable
    private fun ButtonSpacer () {
        return Divider(Modifier.width(Dp(5.0f)))
    }

    /**
     * Display a single name the user can click.
     */
    @Composable
    private fun AssignmentItem(assignment: AssignmentDetails, eventHandler: BotCaptainEvents) {

        Text(text = "Map : ${assignment.mapId}")
        LazyRow {
            items(assignment.steps) { s ->
                // When an item's [name] updates, the adapter for that item
                // will recompose. This will not recompose when [header] changes
                Text (text = "${s.command} : ${s.params}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    @Composable
    private fun MapSelector(
        header: String,
        eventHandler: BotCaptainEvents,
        botCaptainState: BotCaptainState
    ) {
        val headerText =  remember { mutableStateOf(if (botCaptainState.assignmentMap.equals(""))  header else botCaptainState.assignmentMap) }
        eventHandler.setAssignmentMapListener(headerText)
        Log.i("map","Recomposing map selector: Map = ${botCaptainState.assignmentMap}")

        Column (Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            // this will recompose when [header] changes, but not when [names] changes
            Text("Selected Map: " + headerText.value)
            Divider(thickness = Dp(3.0F), color = Color.Transparent)

            AssignmentMapPickerItem(
                mapId = botCaptainState.assignmentMap,
                eventHandler = eventHandler,
                navState = botCaptainState)
        }
    }

    /**
     * Display a single name the user can click.
     */
    @Composable
    private fun AssignmentMapPickerItem(mapId: String, eventHandler: BotCaptainEvents, navState: BotCaptainState) {
        var fontWeight = FontWeight.Normal

        var expanded = remember { mutableStateOf(false) }
        Box {
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                for (mapKey in navState.allMaps.keys) {
                    DropdownMenuItem(
                        text = { Text(mapKey) },
                        onClick = {
                            eventHandler.assignmentMapSelected(mapKey); expanded.value = false
                        }
                    )
                }
            }
            Button(
                content = { Text("Change Map", fontWeight = fontWeight) },
                //colors = buttonColors,
                border = null,
                onClick = { expanded.value = true })
        }
    }

    @Composable
    private fun CoordXPicker(mapId: String, eventHandler: BotCaptainEvents, navState: BotCaptainState) {
        var fontWeight = FontWeight.Normal

        // Find all possible x coordinates
        if (navState.navMap != null) {
            val xmin = navState.navMap.boundaries.xmin.roundToInt()
            val xmax = navState.navMap.boundaries.xmax.roundToInt()
            val stepSize : Int = ((xmax - xmin) / 20)

            var expanded = remember { mutableStateOf(false) }
            Box {
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    for (xval in xmin until xmax step stepSize) {
                        DropdownMenuItem(
                            text = { Text(xval.toString()) },
                            onClick = {
                                // add task to search for the given object
                                updateX(xval.toString()); expanded.value = false
                            }
                        )
                    }
                }
                Button(
                    content = { Text("X:${inputX}", fontWeight = fontWeight) },
                    //colors = buttonColors,
                    border = null,
                    onClick = { expanded.value = true })
            }
        }
    }

    @Composable
    private fun CoordYPicker(mapId: String, eventHandler: BotCaptainEvents, navState: BotCaptainState) {
        var fontWeight = FontWeight.Normal

        // Find all possible x coordinates
        if (navState.navMap != null) {
            val ymin = navState.navMap.boundaries.ymin.roundToInt()
            val ymax = navState.navMap.boundaries.ymax.roundToInt()
            val stepSize : Int = ((ymax - ymin) / 20)

            var expanded = remember { mutableStateOf(false) }
            Box {
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    for (yval in ymin until ymax step stepSize) {
                        DropdownMenuItem(
                            text = { Text(yval.toString()) },
                            onClick = {
                                // add task to search for the given object
                                updateY(yval.toString()); expanded.value = false
                            }
                        )
                    }
                }
                Button(
                    content = { Text("Y:${inputY}", fontWeight = fontWeight) },
                    //colors = buttonColors,
                    border = null,
                    onClick = { expanded.value = true })
            }
        }
    }

    @Composable
    private fun SearchPickerItem(mapId: String, eventHandler: BotCaptainEvents, navState: BotCaptainState) {
        var fontWeight = FontWeight.Normal

        // Find all searchable items on the given map
        if (navState.navMap != null) {
            var expanded = remember { mutableStateOf(false) }
            Box {
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    for (objectType in navState.navMap.search.keys) {
                        DropdownMenuItem(
                            text = { Text(objectType) },
                            onClick = {
                                // add task to search for the given object
                                eventHandler.sendAssignmentSearch(objectType); expanded.value = false
                            }
                        )
                    }
                }
                Button(
                    content = { Text("Search", fontWeight = fontWeight) },
                    //colors = buttonColors,
                    border = null,
                    onClick = { expanded.value = true })
            }
        }
    }

    @Composable
    private fun DriveModePickerItem(eventHandler: BotCaptainEvents) {
        var fontWeight = FontWeight.Normal
        var expanded = remember { mutableStateOf(false) }
        Box {
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Autonomous") },
                    onClick = {
                        eventHandler.sendAssignmentBeginAutonomous(); expanded.value = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Controlled") },
                    onClick = {
                        eventHandler.sendAssignmentBeginControlled(); expanded.value = false
                    }
                )
            }
            Button(
                content = { Text("Drive Mode", fontWeight = fontWeight) },
                //colors = buttonColors,
                border = null,
                onClick = { expanded.value = true })
        }
    }
}
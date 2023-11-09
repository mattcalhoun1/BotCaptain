package com.captain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.captain.components.AppComponents
import com.captain.components.TabComponents
import com.captain.services.navsvc.AssignmentDetails
import com.captain.services.navsvc.BotImage
import com.captain.services.navsvc.Landmark
import com.captain.services.navsvc.MapBoundaries
import com.captain.services.navsvc.NavMap
import com.captain.services.navsvc.BotSession
import com.captain.services.navsvc.Obstacle
import com.captain.services.navsvc.PositionLogEntry
import com.captain.services.navsvc.PositionView
import com.captain.services.navsvc.SearchHit
import com.captain.services.navsvc.Searchable
import com.captain.services.navsvc.Vehicle
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState
import com.captain.ui.theme.BotCaptainTheme

class MainActivity : ComponentActivity() {
    private var botCaptainState  = getBeginningState()
    private val eventHandler = BotCaptainEvents(botCaptainState, this)
    private lateinit var showProgress : MutableState<Boolean>
    private var showProgressState : Boolean = true // this is used when backround threads have shown/hid spinner, but ui has not yet initialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            composeProgressBar()
            BotCaptainTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        AppComponents().AppHeader()
                        Divider()

                        TabComponents().TabContainer(botCaptainState = botCaptainState, eventHandler = eventHandler)
                        Divider()

                        AppComponents().AppFooter(eventHandler = eventHandler)
                    }
                }
            }
        }

        eventHandler.getAllVehicles()
    }

    // Triggers hiding of progress spinner upon next refresh
    fun hideProgressBar() {
        showProgressState = false
        try {
            if (::showProgress.isInitialized) {
                showProgress.value = false
            }
        } catch(ex : Exception) {
            // can happen if app is waking up
        }
    }

    // triggers the progress spinner to show upon next refresh
    fun showProgressBar () {
        showProgressState = true
        try {
            if (::showProgress.isInitialized) {
                showProgress.value = true
            }
        } catch(ex : Exception) {
            // can happen if app is waking up
        }
    }

    @Composable
    private fun composeProgressBar() {
        if (::showProgress.isInitialized == false) {
            showProgress = remember { mutableStateOf(showProgressState) }
        }
        if (showProgress.value) {
            Dialog(
                onDismissRequest = { showProgress.value = false },
                DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(
                    contentAlignment= Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    private fun getBeginningState () : BotCaptainState {
        return BotCaptainState(
            "",
            "",
            ArrayList<BotSession>(),
            ArrayList<Vehicle>(),
            ArrayList<PositionLogEntry>(),
            "",
            ArrayList<SearchHit>(),
            ArrayList<String>(),
            ArrayList<PositionView>(),
            ArrayList<BotImage>(),
            "",
            NavMap(
                landmarks = HashMap<String,Landmark>(),
                boundaries = MapBoundaries(0F,0F,0F,0F),
                shape = "none",
                obstacles = HashMap<String,Obstacle>(),
                search = HashMap<String, Searchable>()),
            HashMap<String,NavMap>(),
            "",
            ArrayList<AssignmentDetails>()
        )
    }
}



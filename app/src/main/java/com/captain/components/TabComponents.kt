package com.captain.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.captain.services.navsvc.BotCaptainEvents
import com.captain.services.navsvc.BotCaptainState
import kotlinx.coroutines.launch

class TabComponents {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TabContainer (botCaptainState: BotCaptainState, eventHandler: BotCaptainEvents) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

        val tabRowItems = listOf(
            TabRowItem(
                title = "Bots",
                screen = { VehicleSelectionComponents().SelectionsContainer(botCaptainState = botCaptainState, eventHandler = eventHandler) },
                //icon = Icons.Rounded.Place,
            ),
            TabRowItem(
                title = "Log",
                screen = { PositionLogComponents().LogContainer(botCaptainState = botCaptainState, eventHandler = eventHandler) },
                //icon = Icons.Rounded.Search,
            ),
            TabRowItem(
                title = "View",
                screen = { BotViewComponents().PositionViewContainer(botCaptainState = botCaptainState, eventHandler = eventHandler) },
                //icon = Icons.Rounded.Star,
            ),
            TabRowItem(
                title = "Map",
                screen = { NavMapComponents().MapContainer(botCaptainState = botCaptainState, eventHandler = eventHandler) },
                //icon = Icons.Rounded.Star,
            ),
            TabRowItem(
                title = "Tasks",
                screen = { AssignmentComponents().AssignmentsContainer(botCaptainState = botCaptainState, eventHandler = eventHandler)}
            )
        )
        Column {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        //Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
            ) {
                tabRowItems.forEachIndexed { index, item ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        //icon = {
                        //    Icon(imageVector = item.icon, contentDescription = "")
                        //},
                        text = {
                            Text(
                                text = item.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    )
                }
            }
            HorizontalPager(
                pageCount = tabRowItems.size,
                state = pagerState,
            ) {
                tabRowItems[pagerState.currentPage].screen()
            }
        }
    }


    data class TabRowItem(
        val title: String,
        //val icon: ImageVector,
        val screen: @Composable () -> Unit,
    )

}
package com.yara.raco.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.yara.raco.ui.RacoScreen

@Composable
fun RacoMainNavHost(
    navHostController: NavHostController,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = RacoScreen.Avisos.name,
        modifier = modifier
    ) {
        composable(RacoScreen.Avisos.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                }
            }
        }

        composable(RacoScreen.Horari.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                }
            }
        }

        composable(RacoScreen.Notes.name) {
            RacoSwipeRefresh(isRefreshing = isRefreshing, onRefresh = onRefresh) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                }
            }
        }
    }
}

@Composable
fun RacoSwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val refreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = refreshState,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicator = { rState, refreshTrigger ->
            SwipeRefreshIndicator(
                state = rState,
                refreshTriggerDistance = refreshTrigger,
                contentColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            )
        }
    ) {
        content()
    }
}
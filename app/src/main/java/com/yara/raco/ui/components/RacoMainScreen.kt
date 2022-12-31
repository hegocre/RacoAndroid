package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yara.raco.R
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.activities.AboutActivity
import com.yara.raco.ui.theme.RacoTheme
import com.yara.raco.ui.viewmodel.RacoViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RacoMainScreen(
    racoViewModel: RacoViewModel,
    notificationNoticeId: Int,
    onLogOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = RacoScreen.fromRoute(
        backStackEntry.value?.destination?.route
    )

    val context = LocalContext.current

    var dayCalendarViewSelected by rememberSaveable { mutableStateOf(true) }

    val currentRoute = backStackEntry.value?.destination?.route
    val onBackPress: (() -> Unit)? = when {
        //Declare back action for button to appear
        currentRoute?.startsWith("${RacoScreen.Grades.name}/details") == true ||
                currentRoute?.startsWith("${RacoScreen.Notes.name}/details") == true -> {
            {
                navController.popBackStack()
            }
        }
        //Default to not visible
        else -> null
    }

    val onEventSettingsPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        RacoScreen.Schedule.name -> {
            {
                dayCalendarViewSelected = !dayCalendarViewSelected
            }
        }
        //Default to not visible
        else -> null
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    var showLogOutDialog by rememberSaveable { mutableStateOf(false) }

    RacoTheme {
        Scaffold(
            topBar = {
                RacoMainTopAppBar(
                    title = stringResource(id = currentScreen.title),
                    scrollBehavior = scrollBehavior,
                    onLogOut = { showLogOutDialog = true },
                    onBackPress = onBackPress,
                    isDayViewSelected = dayCalendarViewSelected,
                    onEventSettingsPress = onEventSettingsPress,
                    onAbout = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    }
                )
            },
            bottomBar = {
                RacoMainNavigationBar(
                    allScreens = RacoScreen.values().toList(),
                    currentScreen = currentScreen,
                    onScreenSelected = { racoScreen ->
                        navController.navigate(racoScreen.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
        ) { paddingValues ->
            RacoMainNavHost(
                navHostController = navController,
                racoViewModel = racoViewModel,
                dayCalendarViewSelected = dayCalendarViewSelected,
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues)
                    .consumedWindowInsets(paddingValues),
            )

            if (showLogOutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogOutDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogOutDialog = false
                            onLogOut()
                        }) {
                            Text(text = stringResource(id = R.string.logout))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogOutDialog = false }) {
                            Text(text = stringResource(id = android.R.string.cancel))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.logout)) },
                    text = { Text(text = stringResource(id = R.string.logout_confirmation)) }
                )
            }
        }
    }

    LaunchedEffect(key1 = notificationNoticeId) {
        if (notificationNoticeId != -1) {
            navController.navigate("${RacoScreen.Notes.name}/details/${notificationNoticeId}")
        }
    }
}
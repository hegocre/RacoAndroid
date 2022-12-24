package com.yara.raco.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import com.yara.raco.ui.RacoScreen
import com.yara.raco.ui.activities.AboutActivity
import com.yara.raco.ui.theme.RacoTheme
import com.yara.raco.ui.viewmodel.RacoViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoMainScreen(
    racoViewModel: RacoViewModel,
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
        currentRoute == "${RacoScreen.Notes.name}/details" || currentRoute?.startsWith("${RacoScreen.Avisos.name}/details") == true -> {
            {
                navController.popBackStack()
            }
        }
        //Default to not visible
        else -> null
    }

    val onEventSettingsPress: (() -> Unit)? = when (backStackEntry.value?.destination?.route) {
        RacoScreen.Horari.name -> {
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

    RacoTheme {
        Scaffold(
            topBar = {
                RacoMainTopAppBar(
                    title = stringResource(id = currentScreen.title),
                    scrollBehavior = scrollBehavior,
                    onLogOut = onLogOut,
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
            contentWindowInsets = WindowInsets.systemBars,
        ) { paddingValues ->
            RacoMainNavHost(
                navHostController = navController,
                racoViewModel = racoViewModel,
                dayCalendarViewSelected = dayCalendarViewSelected,
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues),
            )
        }
    }
}
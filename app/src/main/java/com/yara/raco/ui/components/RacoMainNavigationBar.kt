package com.yara.raco.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yara.raco.ui.RacoScreen

@Composable
fun RacoMainNavigationBar(
    allScreens: List<RacoScreen>,
    currentScreen: RacoScreen,
    onScreenSelected: (RacoScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars
    ) {
        allScreens.forEach { screen ->
            val selected by derivedStateOf { currentScreen == screen }
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.name
                    )
                },
                label = {
                    Text(text = stringResource(screen.title))
                },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                alwaysShowLabel = false
            )
        }
    }
}
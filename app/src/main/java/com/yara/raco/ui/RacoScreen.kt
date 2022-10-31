package com.yara.raco.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.yara.raco.R

enum class RacoScreen(
    @StringRes val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Avisos(
        title = R.string.avisos,
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    ),
    Horari(
        title = R.string.horari,
        selectedIcon = Icons.Filled.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    ),
    Notes(
        title = R.string.notes,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    );

    companion object {
        fun fromRoute(route: String?): RacoScreen =
            when (route?.substringBefore("/")) {
                Avisos.name -> Avisos
                Horari.name -> Horari
                Notes.name -> Notes
                null -> Avisos
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}
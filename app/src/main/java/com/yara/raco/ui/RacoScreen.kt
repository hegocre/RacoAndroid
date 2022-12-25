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
    Notes(
        title = R.string.notices,
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    ),
    Schedule(
        title = R.string.planning,
        selectedIcon = Icons.Filled.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    ),
    Grades(
        title = R.string.grades,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    );

    companion object {
        fun fromRoute(route: String?): RacoScreen =
            when (route?.substringBefore("/")) {
                Notes.name -> Notes
                Schedule.name -> Schedule
                Grades.name -> Grades
                null -> Notes
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}
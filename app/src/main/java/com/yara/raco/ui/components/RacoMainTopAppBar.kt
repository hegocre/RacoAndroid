package com.yara.raco.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.yara.raco.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoMainTopAppBar(
    title: String,
    onLogOut: () -> Unit,
    onAbout: () -> Unit,
    onBackPress: (() -> Unit)? = null,
    onEventSettingsPress: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var menuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var calendarMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.statusBars,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
        navigationIcon = {
            AnimatedVisibility(
                visible = onBackPress != null,
                enter = slideIn(tween(), initialOffset = { offset -> IntOffset(-offset.width, 0) }),
                exit = slideOut(tween(), targetOffset = { offset -> IntOffset(-offset.width, 0) })
            ) {
                IconButton(onClick = { onBackPress?.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(
                            id = R.string.back
                        )
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = onEventSettingsPress != null,
                enter = slideIn(tween(), initialOffset = { offset -> IntOffset(-offset.width, 0) }),
                exit = slideOut(tween(), targetOffset = { offset -> IntOffset(-offset.width, 0) })
            ) {
                IconButton(onClick = { calendarMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Event, contentDescription = stringResource(
                            id = R.string.calendar_menu
                        )
                    )
                }
                DropdownMenu(expanded = calendarMenuExpanded, onDismissRequest = { calendarMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(text= stringResource(id = R.string.calendar_day)) },
                        onClick = {
                            onEventSettingsPress?.invoke()
                            calendarMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text= stringResource(id = R.string.calendar_week)) },
                        onClick = {
                            onEventSettingsPress?.invoke()
                            calendarMenuExpanded = false
                        }
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, contentDescription = stringResource(
                            id = R.string.menu
                        )
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.logout)) },
                        onClick = {
                            onLogOut()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.about)) },
                        onClick = {
                            onAbout()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    )
}
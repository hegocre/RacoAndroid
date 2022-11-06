package com.yara.raco.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.yara.raco.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoMainTopAppBar(
    title: String,
    onLogOut: () -> Unit,
    onAbout: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var menuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets.statusBars,
        actions = {
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
                        text = { Text(text = stringResource(id = R.string.tancar_sessio)) },
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
package com.yara.raco.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.yara.raco.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoMainTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackPress: (() -> Unit)? = null,
    iconActions: Map<Pair<ImageVector, String>, () -> Unit>? = null,
    dropdownActions: Map<String, () -> Unit>? = null,
) {
    var menuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    TopAppBar(
        title = { Text(text = title) },
        windowInsets = WindowInsets.statusBars,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
        navigationIcon = {
            if (onBackPress != null) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(
                            id = R.string.back
                        )
                    )
                }
            }
        },
        actions = {
            iconActions?.forEach { (imageVector, function) ->
                RichTooltipBox(text = { Text(text = imageVector.second) }) {
                    IconButton(onClick = function, modifier = Modifier.tooltipAnchor()) {
                        Icon(
                            imageVector = imageVector.first,
                            contentDescription = imageVector.second
                        )
                    }
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
                dropdownActions?.let {
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }) {
                        it.forEach { (title, function) ->
                            DropdownMenuItem(
                                text = { Text(text = title) },
                                onClick = {
                                    function()
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
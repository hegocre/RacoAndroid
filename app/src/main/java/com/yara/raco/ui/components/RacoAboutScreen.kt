package com.yara.raco.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yara.raco.R
import com.yara.raco.ui.theme.RacoTheme

data class LicenseNotice(
    val name: String,
    val copyright: String,
    val licenseName: String,
    val licenseUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoAboutScreen(
    onBackPressed: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    var showLicensesDialog by rememberSaveable { mutableStateOf(false) }

    RacoTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.about))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    windowInsets = WindowInsets.statusBars
                )
            },
            contentWindowInsets = WindowInsets.systemBars
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        OutlinedCard(
                            border = CardDefaults.outlinedCardBorder(enabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .height(50.dp)
                                            .width(50.dp)
                                            .clip(CircleShape),
                                        painter = painterResource(id = R.drawable.app_icon),
                                        contentDescription = stringResource(id = R.string.app_name)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = stringResource(id = R.string.version)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.version)) },
                                    secondaryText = {
                                        Text(
                                            text = "v${stringResource(id = R.string.version_name)} " +
                                                    "(${stringResource(id = R.string.version_code)})"
                                        )
                                    }
                                )
                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Code,
                                            contentDescription = stringResource(id = R.string.source_code)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.source_code)) },
                                    onClick = { uriHandler.openUri(repoUrl) }
                                )

                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Campaign,
                                            contentDescription = stringResource(id = R.string.help_suggestions)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.help_suggestions)) },
                                    onClick = { uriHandler.openUri("$repoUrl/issues") }
                                )

                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = stringResource(id = R.string.licenses)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.licenses)) },
                                    onClick = { showLicensesDialog = true }
                                )

                                val policyUrl = stringResource(id = R.string.privacy_policy_url)
                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Policy,
                                            contentDescription = stringResource(id = R.string.privacy_policy)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.privacy_policy)) },
                                    onClick = { uriHandler.openUri(policyUrl) }
                                )
                            }
                        }
                    }

                    //Authors card
                    item {
                        OutlinedCard(
                            border = CardDefaults.outlinedCardBorder(enabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.authors),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                )

                                for (author in authors) {
                                    AboutTextField(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = stringResource(id = R.string.version)
                                            )
                                        },
                                        primaryText = { Text(text = author.key) },
                                        onClick = { uriHandler.openUri(author.value) }
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(id = R.string.api_raco_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .clickable { uriHandler.openUri(apiUrl) }
                )
            }

            if (showLicensesDialog) {
                LicensesDialog(
                    licenses = licenses,
                    onDismissRequest = { showLicensesDialog = false }
                )
            }
        }
    }
}

@Composable
fun AboutTextField(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    primaryText: (@Composable () -> Unit)? = null,
    secondaryText: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentColor provides LocalContentColor.current.copy(alpha = 0.7f),
        ) {
            icon?.invoke()
        }
        Column(modifier = Modifier.padding(start = if (icon == null) 0.dp else 24.dp)) {
            primaryText?.let { content ->
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyLarge
                ) {
                    content()
                }
            }
            secondaryText?.let { content ->
                CompositionLocalProvider(
                    LocalContentColor provides LocalContentColor.current.copy(alpha = 0.7f),
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun LicensesDialog(
    licenses: List<LicenseNotice>,
    onDismissRequest: (() -> Unit)? = null
) {

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    text = stringResource(id = R.string.licenses),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 24.dp)
                )

                val uriHandler = LocalUriHandler.current

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(items = licenses, key = { it.name }) { license ->
                        ListItem(
                            headlineContent = {
                                Text(text = license.name)
                            },
                            overlineContent = {
                                Text(text = license.licenseName)
                            },
                            supportingContent = {
                                Text(text = license.copyright)
                            },
                            modifier = Modifier
                                .clickable {
                                    uriHandler.openUri(license.licenseUrl)
                                }
                                .padding(horizontal = 10.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { onDismissRequest?.invoke() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        }
    }
}

const val apiUrl = "https://api.fib.upc.edu/v2/"
const val repoUrl = "https://github.com/hegocre/RacoAndroid"
val authors = mapOf(
    "Oriol Deiros" to "https://github.com/oriol366",
    "Ignasi Fibla" to "https://github.com/IFibla",
    "Hector Godoy" to "https://github.com/hegocre",
)
val licenses = listOf(
    LicenseNotice(
        name = "Kotlin Programming Language",
        copyright = "Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt"
    ),
    LicenseNotice(
        name = "Android Jetpack",
        copyright = "Copyright 2020 Google LLC",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt"
    ),
    LicenseNotice(
        name = "Accompanist",
        copyright = "Copyright 2020 The Android Open Source Project",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/google/accompanist/blob/main/LICENSE"
    ),
    LicenseNotice(
        name = "HtmlText",
        copyright = "Copyright 2021 Alexander Karkossa",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/ch4rl3x/HtmlText/blob/main/LICENSE"
    ),
    LicenseNotice(
        name = "OkHttp",
        copyright = "Copyright 2019 Square, Inc.",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/square/okhttp/blob/master/LICENSE.txt"
    ),
    LicenseNotice(
        name = "Compose Tooltip",
        copyright = "Copyright 2022 Patrick Goldinger",
        licenseName = "Apache License 2.0",
        licenseUrl = "https://github.com/patrickgold/compose-tooltip/blob/main/LICENSE"
    ),
    LicenseNotice(
        name = "WeekSchedule",
        copyright = "Copyright 2021 Daniel Rampelt",
        licenseName = "MIT License",
        licenseUrl = "https://github.com/drampelt/WeekSchedule/blob/main/LICENSE"
    )
)

@Preview
@Composable
fun RacoAboutPreview() {
    RacoAboutScreen {

    }
}
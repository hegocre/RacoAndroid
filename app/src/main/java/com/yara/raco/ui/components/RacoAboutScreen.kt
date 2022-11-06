package com.yara.raco.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yara.raco.R
import com.yara.raco.ui.theme.RacoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacoAboutScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current

    RacoTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
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
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(repoUrl)
                                        context.startActivity(intent)
                                    }
                                )

                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Campaign,
                                            contentDescription = stringResource(id = R.string.help_suggestions)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.help_suggestions)) },
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse("$repoUrl/issues")
                                        context.startActivity(intent)
                                    }
                                )

                                AboutTextField(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = stringResource(id = R.string.licenses)
                                        )
                                    },
                                    primaryText = { Text(text = stringResource(id = R.string.licenses)) },
                                    onClick = {}
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
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.data = Uri.parse(author.value)
                                            context.startActivity(intent)
                                        }
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
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(apiUrl)
                            context.startActivity(intent)
                        }
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

const val apiUrl = "https://api.fib.upc.edu/v2/"
const val repoUrl = "https://github.com/hegocre/RacoAndroid"
val authors = mapOf(
    "Oriol Deiros" to "https://github.com/oriol366",
    "Ignasi Fibla" to "https://github.com/IFibla",
    "Hector Godoy" to "https://github.com/hegocre",
)

@Preview
@Composable
fun RacoAboutPreview() {
    RacoAboutScreen {

    }
}
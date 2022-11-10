package com.yara.raco.ui.components

import android.text.format.DateUtils
import android.text.format.Formatter.formatShortFileSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.ui.theme.RacoTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RacoNoticeList(
    noticesWithFiles: List<NoticeWithFiles>,
    onFileClick: (File) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = noticesWithFiles) { noticeWithFiles ->
            NoticeWithFiles(
                noticeWithFiles = noticeWithFiles,
                onFileClick = onFileClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeWithFiles(
    noticeWithFiles: NoticeWithFiles,
    onFileClick: (File) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault())
    val date =
        dateFormat.parse(noticeWithFiles.notice.dataModificacio) ?: Date(System.currentTimeMillis())
    val dateString = DateUtils.getRelativeTimeSpanString(
        date.time,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )
    val parsedTitle = HtmlCompat.fromHtml(
        noticeWithFiles.notice.titol,
        FROM_HTML_OPTION_USE_CSS_COLORS
    ).toString()

    var showDetails by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { showDetails = !showDetails }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = parsedTitle, style = MaterialTheme.typography.titleMedium
            )
            AnimatedVisibility(visible = showDetails) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    if (noticeWithFiles.notice.text.isNotBlank()) {
                        HtmlText(
                            text = noticeWithFiles.notice.text,
                        )
                    }
                    if (noticeWithFiles.files.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            noticeWithFiles.files.sortedBy { it.dataModificacio }.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onFileClick(file) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = file.nom,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    val fileSize = formatShortFileSize(
                                        LocalContext.current, file.mida.toLong()
                                    )
                                        .replace("(", "")
                                        .replace(")", "")
                                    Text(text = "${file.nom} ($fileSize)")
                                }
                            }
                        }
                    }
                }
            }
            val codiSub = if (noticeWithFiles.notice.codiAssig.startsWith("#"))
                "FIB" else noticeWithFiles.notice.codiAssig
            Text(text = "$codiSub - $dateString")

        }
    }
}

@Preview
@Composable
fun NoticeWithFilesPreview() {
    RacoTheme {
        NoticeWithFiles(
            noticeWithFiles = NoticeWithFiles(
                notice = Notice(
                    id = 0,
                    titol = "Avís de prova",
                    codiAssig = "FIB",
                    text = "Aquest es un avís de prova<br><br>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Id semper risus in hendrerit gravida rutrum quisque non.<br><br>Lectus nulla at volutpat diam ut venenatis tellus. Facilisi etiam dignissim diam quis. A arcu cursus vitae congue mauris.",
                    dataInsercio = "2021-02-15T00:00:00",
                    dataModificacio = "2021-02-15T00:00:00",
                    dataCaducitat = "2023-02-15T00:00:00"
                ),
                files = listOf(
                    File(
                        tipusMime = "application/pdf",
                        nom = "DocumentSuperInteressant.pdf",
                        url = "",
                        dataModificacio = "2022-10-11T15:19:04",
                        mida = 182374681,
                    ),
                    File(
                        tipusMime = "application/pdf",
                        nom = "AquestNoTant.pdf",
                        url = "",
                        dataModificacio = "2022-10-11T15:19:04",
                        mida = 87238746,
                    )
                )
            ),
            onFileClick = {}
        )
    }
}
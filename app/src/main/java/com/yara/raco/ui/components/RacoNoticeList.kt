package com.yara.raco.ui.components

import android.text.format.DateUtils
import android.text.format.Formatter.formatShortFileSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.yara.raco.R
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject
import com.yara.raco.ui.theme.RacoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoNoticeTabs(
    subjects: List<Subject>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        Spacer(
            Modifier
                .pagerTabIndicatorOffset(pagerState, tabPositions)
                .padding(horizontal = 12.dp)
                .height(3.dp)
                .background(
                    LocalContentColor.current,
                    RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)
                )
        )
    }

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = modifier,
        indicator = indicator,
        edgePadding = 32.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        Tab(
            selected = pagerState.currentPage == 0,
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0)
                }
            },
            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            text = { Text(text = stringResource(id = R.string.all)) }
        )
        subjects.forEachIndexed { index, subject ->
            Tab(
                selected = pagerState.currentPage == index + 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index + 1)
                    }
                },
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                text = { Text(text = subject.sigles) }
            )
        }

    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RacoNoticePager(
    pagerState: PagerState,
    subjects: List<Subject>,
    noticesWithFiles: List<NoticeWithFiles>,
    onFileClick: (File) -> Unit
) {
    HorizontalPager(
        count = subjects.size + 1,
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        if (page == 0) {
            RacoNoticeList(
                noticesWithFiles = noticesWithFiles,
                selectedSubject = null,
                onFileClick = onFileClick
            )
        } else {
            RacoNoticeList(
                noticesWithFiles = noticesWithFiles,
                selectedSubject = subjects.getOrNull(page - 1),
                onFileClick = onFileClick
            )
        }
    }
}

@Composable
fun RacoNoticeList(
    noticesWithFiles: List<NoticeWithFiles>,
    selectedSubject: Subject?,
    onFileClick: (File) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val noticeListState = rememberLazyListState()
    LaunchedEffect(key1 = noticesWithFiles.firstOrNull()) {
        noticeListState.animateScrollToItem(0)
    }
    val noticesWithFilesFiltered = remember(noticesWithFiles) {
        noticesWithFiles.filter {
            if (selectedSubject == null) true else it.notice.codiAssig == selectedSubject.id
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = noticeListState,
    ) {
        if (noticesWithFilesFiltered.isNotEmpty()) {
            itemsIndexed(
                items = noticesWithFilesFiltered,
                key = { _, noticeWithFiles -> noticeWithFiles.notice.id }
            ) { index, noticeWithFiles ->
                NoticeWithFiles(
                    noticeWithFiles = noticeWithFiles,
                    onNoticeClose = {
                        if (noticeListState.firstVisibleItemIndex == index) {
                            coroutineScope.launch {
                                noticeListState.animateScrollToItem(index)
                            }
                        }
                    },
                    onFileClick = onFileClick,
                )
            }
        } else {
            item(key = "no_items") {
                Text(
                    text = stringResource(id = R.string.no_notices_yet),
                    modifier = Modifier
                        .fillParentMaxSize()
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeWithFiles(
    noticeWithFiles: NoticeWithFiles,
    onNoticeClose: () -> Unit,
    onFileClick: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString by produceState(initialValue = "") {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault())
        val date =
            dateFormat.parse(noticeWithFiles.notice.dataModificacio)
                ?: Date(System.currentTimeMillis())
        value = DateUtils.getRelativeTimeSpanString(
            date.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
    val parsedTitle =
        remember { fromHtml(noticeWithFiles.notice.titol, FROM_HTML_MODE_COMPACT).toString() }

    var showDetails by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = {
            showDetails = !showDetails
            if (!showDetails)
                onNoticeClose()
        }
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
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            val sortedFiles =
                                remember { noticeWithFiles.files.sortedBy { it.dataModificacio } }
                            sortedFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onFileClick(file) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = file.nom,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    val fileSize = remember {
                                        formatShortFileSize(context, file.mida.toLong())
                                            .replace("(", "")
                                            .replace(")", "")
                                    }
                                    Text(text = "${file.nom} ($fileSize)")
                                }
                            }
                        }
                    }
                }
            }
            val codiSub = remember(noticeWithFiles.notice) {
                if (noticeWithFiles.notice.codiAssig.startsWith("#"))
                    "FIB" else noticeWithFiles.notice.codiAssig
            }
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
            onFileClick = {},
            onNoticeClose = {}
        )
    }
}

@Preview
@Composable
fun RacoNoticeListPreview() {
    RacoTheme {
        RacoNoticeList(
            noticesWithFiles = emptyList(),
            selectedSubject = null,
            onFileClick = {}
        )
    }
}
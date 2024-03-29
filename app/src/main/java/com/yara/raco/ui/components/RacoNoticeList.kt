package com.yara.raco.ui.components

import android.text.Html
import android.text.format.DateUtils
import android.text.format.Formatter.formatShortFileSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.fromHtml
import com.yara.raco.R
import com.yara.raco.model.files.File
import com.yara.raco.model.notices.Notice
import com.yara.raco.model.notices.NoticeWithFiles
import com.yara.raco.model.subject.Subject
import com.yara.raco.ui.components.htmltext.HtmlText
import com.yara.raco.ui.components.pager.pagerTabIndicatorOffset
import com.yara.raco.ui.theme.RacoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
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

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = BottomCenter) {
        Divider(modifier = Modifier.fillMaxWidth())

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = indicator,
            divider = {},
            containerColor = Color.Transparent,
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RacoNoticePager(
    showAllNoticesSelected: Boolean,
    pagerState: PagerState,
    subjects: List<Subject>,
    noticesWithFiles: List<NoticeWithFiles>,
    onNoticeClick: (NoticeWithFiles) -> Unit
) {
    val filteredNotices = remember(showAllNoticesSelected, noticesWithFiles) {
        noticesWithFiles.filter {
            if (!showAllNoticesSelected) !it.notice.llegit else true
        }
    }

    HorizontalPager(state = pagerState) { page ->
        if (page == 0) {
            RacoNoticeList(
                noticesWithFiles = filteredNotices,
                selectedSubject = null,
                onNoticeClick = onNoticeClick
            )
        } else {
            RacoNoticeList(
                noticesWithFiles = filteredNotices,
                selectedSubject = subjects.getOrNull(page - 1),
                onNoticeClick = onNoticeClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RacoNoticeList(
    noticesWithFiles: List<NoticeWithFiles>,
    selectedSubject: Subject?,
    onNoticeClick: (NoticeWithFiles) -> Unit
) {
    val noticeListState = rememberLazyListState()
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
                if (index != 0) {
                    Divider()
                }
                NoticeListEntry(
                    noticeWithFiles = noticeWithFiles,
                    onNoticeClick = onNoticeClick,
                    modifier = Modifier.animateItemPlacement()
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

@Composable
fun NoticeListEntry(
    noticeWithFiles: NoticeWithFiles,
    onNoticeClick: (NoticeWithFiles) -> Unit,
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

    val codiSub = remember(noticeWithFiles.notice) {
        if (noticeWithFiles.notice.codiAssig.startsWith("#"))
            "FIB" else noticeWithFiles.notice.codiAssig
    }

    val noticeText = noticeWithFiles.notice.text
    ListItem(
        modifier = modifier.clickable {
            onNoticeClick(noticeWithFiles)
        },
        headlineContent = {
            Text(
                text = buildAnnotatedString {
                    append(parsedTitle)
                    if (!noticeWithFiles.notice.llegit) {
                        withStyle(
                            LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary)
                                .toSpanStyle()
                        ) {
                            append(" ⦁")
                        }
                    }
                },
                fontWeight = if (noticeWithFiles.notice.llegit) FontWeight.Normal else FontWeight.ExtraBold
            )
        },
        supportingContent = if (noticeText.isNotBlank()) {
            {
                if (noticeText.isNotBlank()) {
                    if (noticeText.isHtml()) {
                        Text(
                            text = Html.fromHtml(noticeText, Html.FROM_HTML_MODE_COMPACT)
                                .toString(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = noticeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else null,
        overlineContent = {
            val attachmentInlineContent = mapOf(
                Pair(
                    "attachment_icon",
                    InlineTextContent(
                        placeholder = Placeholder(
                            width = LocalTextStyle.current.fontSize,
                            height = LocalTextStyle.current.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = stringResource(
                                id = R.string.attachments
                            )
                        )
                    }
                )
            )

            Text(
                text = buildAnnotatedString {
                    append("$codiSub - $dateString")
                    if (noticeWithFiles.files.isNotEmpty()) {
                        append(" -")
                        appendInlineContent("attachment_icon")
                        append("${noticeWithFiles.files.size}")
                    }
                },
                inlineContent = attachmentInlineContent
            )
        }
    )
}

@Composable
fun DetailedNoticeWithFiles(
    noticeWithFiles: NoticeWithFiles,
    onFileClick: (File) -> Unit,
) {
    val parsedTitle =
        remember { fromHtml(noticeWithFiles.notice.titol, FROM_HTML_MODE_COMPACT).toString() }

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

    val codiSub = remember(noticeWithFiles.notice) {
        if (noticeWithFiles.notice.codiAssig.startsWith("#"))
            "FIB" else noticeWithFiles.notice.codiAssig
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "$codiSub - $dateString",
            style = MaterialTheme.typography.labelLarge,
            //modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(text = parsedTitle, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        val noticeText = noticeWithFiles.notice.text
        if (noticeText.isNotBlank()) {
            if (noticeText.isHtml()) {
                HtmlText(
                    text = noticeText,
                )
            } else {
                Text(
                    text = noticeText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
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
                        val context = LocalContext.current
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
    Column(modifier = Modifier.padding(12.dp)) {
    }
}

@Preview
@Composable
fun NoticeWithFilesPreview() {
    RacoTheme {
        NoticeListEntry(
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
            onNoticeClick = {}
        )
    }
}

fun String.isHtml(): Boolean = ("<[^>]+>".toRegex(RegexOption.IGNORE_CASE).containsMatchIn(this))

@Preview
@Composable
fun RacoNoticeListPreview() {
    RacoTheme {
        RacoNoticeList(
            noticesWithFiles = emptyList(),
            selectedSubject = null,
            onNoticeClick = {}
        )
    }
}
package com.easylists.presentation.ui.notices

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylists.domain.models.ListNotice
import com.easylists.domain.models.NoticeKind
import com.easylists.presentation.R
import com.easylists.presentation.models.Screen
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.popUpTo

// The popup for shared lists: one that was taken away, or someone who left. It
// sits at the root of the app, next to the NavHost, so it shows over whichever
// screen is open.
@Composable
fun ListNoticeHost(
    viewModel: ListNoticeViewModel,
    navController: NavController<Screen>,
) {
    val notices by viewModel.notices.collectAsStateWithLifecycle()

    // Looking at a list that was just taken away: back to the lists screen
    LaunchedEffect(notices) {
        val top = navController.backstack.entries.last().destination
        if (top is Screen.ListItems &&
            notices.any { it.kind == NoticeKind.Gone && it.listId == top.listId }
        ) {
            navController.popUpTo { it == Screen.Lists }
        }
    }

    if (notices.isNotEmpty()) {
        // What is on screen now, so OK clears exactly these
        val shown = notices
        AlertDialog(
            onDismissRequest = { viewModel.onDismiss(shown) },
            title = { Text(stringResource(titleRes(shown))) },
            text = { Text(noticeText(shown)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismiss(shown) }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}


private fun titleRes(notices: List<ListNotice>): Int = when {
    notices.size > 1 -> R.string.list_notices_title
    notices.first().kind == NoticeKind.Left -> R.string.list_left_title
    else -> R.string.list_gone_title
}

@Composable
private fun noticeText(notices: List<ListNotice>): String {
    if (notices.size == 1) {
        val notice = notices.first()
        // A copy, because the property comes from another module
        val person = notice.personName
        return when {
            notice.kind == NoticeKind.Gone ->
                stringResource(R.string.list_gone_message_one, notice.listName)

            person.isNullOrBlank() ->
                stringResource(R.string.list_left_message_unnamed, notice.listName)

            else ->
                stringResource(R.string.list_left_message_named, person, notice.listName)
        }
    }
    val lines = notices.map { noticeLine(it) }
    return lines.joinToString("\n") { "• $it" }
}

@Composable
private fun noticeLine(notice: ListNotice): String {
    val person = notice.personName
    return when {
        notice.kind == NoticeKind.Gone ->
            stringResource(R.string.list_gone_line, notice.listName)

        person.isNullOrBlank() ->
            stringResource(R.string.list_left_line_unnamed, notice.listName)

        else ->
            stringResource(R.string.list_left_line_named, person, notice.listName)
    }
}

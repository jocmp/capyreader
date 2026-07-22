package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.common.Media
import com.capyreader.app.common.Saver
import com.capyreader.app.ui.LocalConnectivity
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.audio.AudioPlayerController
import com.capyreader.app.ui.articles.detail.ArticleView
import com.capyreader.app.ui.articles.detail.CapyPlaceholder
import com.capyreader.app.ui.articles.list.LabelBottomSheet
import com.capyreader.app.ui.provideLinkOpener
import com.capyreader.app.ui.rememberLocalConnectivity
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Content of the [com.capyreader.app.ui.Route.ArticleDetail] entry. Renders the reader for a single
 * article, backed by its own [ArticleViewModel]. Independent of the list entry; neighbor ids for
 * the next/previous reader navigation are supplied by the caller.
 */
@Composable
fun ArticleDetailScreen(
    articleID: String,
    searchQuery: String?,
    onBackPressed: () -> Unit,
    onSelectArticle: (id: String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    viewModel: ArticleViewModel = koinViewModel(),
) {
    LaunchedEffect(articleID) {
        viewModel.load(articleID, searchQuery = searchQuery)
    }

    val context = LocalContext.current
    val article = viewModel.article
    val canSaveExternally by viewModel.canSaveArticleExternally.collectAsStateWithLifecycle()
    val savedSearches by viewModel.savedSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val connectivity = rememberLocalConnectivity()

    val fullContent = remember(viewModel) {
        FullContentFetcher(
            fetch = viewModel::fetchFullContentAsync,
            reset = viewModel::resetFullContent,
        )
    }

    val articleActions = remember(viewModel) {
        ArticleActions(saveExternally = viewModel::saveArticleExternallyAsync)
    }

    var labelSheetArticleID by remember { mutableStateOf<String?>(null) }
    val articleLabels by viewModel.getArticleLabels(labelSheetArticleID)
        .collectAsState(initial = emptyList())

    val labelsActions = remember(savedSearches, labelSheetArticleID, articleLabels) {
        LabelsActions(
            source = viewModel.source,
            showLabels = viewModel.source.supportsLabels,
            savedSearches = savedSearches,
            selectedArticleID = labelSheetArticleID,
            articleLabels = articleLabels,
            openSheet = { labelSheetArticleID = it },
            closeSheet = { labelSheetArticleID = null },
            addLabel = viewModel::addLabelAsync,
            removeLabel = viewModel::removeLabelAsync,
            createLabel = viewModel::createLabel,
        )
    }

    val audioController: AudioPlayerController = koinInject()
    val isAudioPlaying by audioController.isPlaying.collectAsState()
    val currentAudio by audioController.currentAudio.collectAsState()

    val paneExpansion = LocalArticlePaneExpansion.current

    CompositionLocalProvider(
        LocalFullContent provides fullContent,
        LocalArticleActions provides articleActions,
        LocalLabelsActions provides labelsActions,
        LocalConnectivity provides connectivity,
        LocalLinkOpener provides provideLinkOpener(context),
    ) {
        val current = article

        if (current == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CapyPlaceholder()
            }
        } else {
            ArticleView(
                article = current,
                previousArticleID = viewModel.previousArticleID,
                nextArticleID = viewModel.nextArticleID,
                onBackPressed = onBackPressed,
                onToggleRead = viewModel::toggleArticleRead,
                onToggleStar = viewModel::toggleArticleStar,
                canSaveExternally = canSaveExternally,
                onDeletePage = {
                    onBackPressed()
                    viewModel.deletePage(current.id)
                },
                onSelectMedia = onSelectMedia,
                onSelectAudio = { audio -> audioController.play(audio) },
                onPauseAudio = { audioController.pause() },
                onSelectArticle = onSelectArticle,
                currentAudioUrl = currentAudio?.url,
                isAudioPlaying = isAudioPlaying,
                isFullscreen = paneExpansion?.isFullscreen ?: false,
                onToggleFullscreen = { paneExpansion?.toggleFullscreen() },
            )

            labelSheetArticleID?.let { id ->
                LabelBottomSheet(
                    articleID = id,
                    savedSearches = labelsActions.savedSearches,
                    articleLabels = labelsActions.articleLabels,
                    onAddLabel = { savedSearchID -> labelsActions.addLabel(id, savedSearchID) },
                    onRemoveLabel = { savedSearchID -> labelsActions.removeLabel(id, savedSearchID) },
                    onCreateLabel = labelsActions.createLabel,
                    onDismissRequest = labelsActions.closeSheet,
                )
            }
        }
    }
}

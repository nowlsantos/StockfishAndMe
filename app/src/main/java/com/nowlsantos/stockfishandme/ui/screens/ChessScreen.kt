package com.nowlsantos.stockfishandme.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.Difficulty
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.presentation.GameViewModel
import com.nowlsantos.stockfishandme.ui.components.CitationToast
import com.nowlsantos.stockfishandme.ui.components.EvaluationBar
import com.nowlsantos.stockfishandme.ui.core.BoardInformation
import com.nowlsantos.stockfishandme.ui.core.ChessBoard
import com.nowlsantos.stockfishandme.ui.dialogs.DialogManager
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChessScreen(
    perspective: PieceColor,
    depth: Int,
    boardTheme: BoardTheme = BoardTheme.GREEN,
    viewModel: GameViewModel = koinViewModel()
) {
    val uiState by viewModel.gameState.collectAsStateWithLifecycle()
    val isEngineThinking by viewModel.isEngineThinking.collectAsStateWithLifecycle()

    var showCitationToast by remember { mutableStateOf(false) }
    val citationDetails = stringResource(R.string.citation_details)

    LaunchedEffect(Unit) {
        viewModel.startNewGame(
            playerColor = perspective,
            difficulty = Difficulty.entries.find { it.depth == depth } ?: Difficulty.entries.minByOrNull { kotlin.math.abs(it.depth - depth) } ?: Difficulty.NATIONAL_MASTER,
            boardTheme = boardTheme
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepDarkBackground)
        ) {
            // Extreme Left: Evaluation Bar
            EvaluationBar(
                evaluation = uiState.evaluation,
                perspective = perspective,
                boardTheme = uiState.boardTheme
            )

            // Left: Chessboard
            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ChessBoard(
                    uiState = uiState,
                    perspective = perspective,
                    onTileClick = { row, col ->
                        viewModel.onSquareClicked(row, col)
                    },
                    onMakeMove = { fromRow, fromCol, toRow, toCol, isDrag ->
                        viewModel.makeMove(fromRow, fromCol, toRow, toCol, isDrag)
                    }
                )
            }

            // Right: Information & Controls
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
            ) {
                BoardInformation(
                    uiState = uiState,
                    perspective = perspective,
                    isEngineThinking = isEngineThinking,
                    viewModel = viewModel,
                    onReset = { viewModel.resetGame() }
                )
            }
        }

        if (showCitationToast) {
            CitationToast(
                citationText = citationDetails,
                isVisible = true,
                onDismiss = { showCitationToast = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .zIndex(2f)
            )
        }

        uiState.toastMessageRes?.let { resId ->
            val context = LocalContext.current
            val message = stringResource(resId)
            LaunchedEffect(resId) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }

        DialogManager(
            uiState = uiState,
            onPieceSelected = { pieceType -> viewModel.promotePawn(pieceType) },
            onNewGame = { viewModel.resetGame() },
            onConfirmResign = { viewModel.confirmResign() },
            onCancelResign = { viewModel.cancelResign() },
            onDismissDialogs = { viewModel.dismissDialogs() },
            onFinalizeDrawDecision = { viewModel.finalizeDrawDecision() }
        )
    }
}

package com.nowlsantos.stockfishandme.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.DialogType
import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.PieceType

@Composable
fun DialogManager(
    uiState: GameState,
    onPieceSelected: (PieceType) -> Unit,
    onNewGame: () -> Unit,
    onConfirmResign: () -> Unit,
    onCancelResign: () -> Unit,
    onDismissDialogs: () -> Unit,
    onFinalizeDrawDecision: () -> Unit
) {
    AnimatedVisibility(
        visible = uiState.activeDialog != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (uiState.activeDialog) {
            DialogType.DRAW_DECISION -> {
                DrawDecisionDialog(
                    messageRes = uiState.drawDecisionMessageRes ?: R.string.offer_draw_title,
                    onDismiss = onFinalizeDrawDecision
                )
            }
            DialogType.PROMOTION -> {
                PromotionDialog(
                    color = uiState.currentTurn,
                    onPieceSelected = onPieceSelected,
                    onDismiss = onDismissDialogs
                )
            }
            DialogType.RESIGN -> {
                PlayerResignDialog(
                    onResign = onConfirmResign,
                    onCancel = onCancelResign,
                    boardTheme = uiState.boardTheme
                )
            }
            DialogType.GAME_OVER -> {
                GameOverDialog(
                    uiState = uiState,
                    boardTheme = uiState.boardTheme,
                    onNewGame = onNewGame,
                    onDismiss = onDismissDialogs
                )
            }
            else -> {}
        }
    }
}

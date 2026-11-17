package com.nowlsantos.stockfishandme.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.GameStatus
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.ui.components.OverlayDialogWrapper
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.darkColor
import com.nowlsantos.stockfishandme.ui.theme.lightColor

@Composable
fun GameOverDialog(
    uiState: GameState,
    boardTheme: BoardTheme,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = when (uiState.gameStatus) {
        GameStatus.CHECKMATE -> {
            val winnerColor = if (uiState.currentTurn == PieceColor.WHITE) {
                stringResource(R.string.piece_color_black)
            } else {
                stringResource(R.string.piece_color_white)
            }
            stringResource(R.string.game_status_checkmate, winnerColor)
        }
        GameStatus.RESIGNED -> {
            val resignedColor = when (uiState.resignedPlayer) {
                PieceColor.WHITE -> stringResource(R.string.piece_color_white)
                PieceColor.BLACK -> stringResource(R.string.piece_color_black)
                null -> if (uiState.currentTurn == PieceColor.WHITE) {
                    stringResource(R.string.piece_color_white)
                } else {
                    stringResource(R.string.piece_color_black)
                }
            }
            stringResource(R.string.game_outcome_resigned, resignedColor)
        }
        GameStatus.DRAW -> stringResource(R.string.game_outcome_draw)
        GameStatus.TIMEOUT -> {
            val winnerColor = if (uiState.currentTurn == PieceColor.WHITE) {
                stringResource(R.string.piece_color_black)
            } else {
                stringResource(R.string.piece_color_white)
            }
            stringResource(R.string.game_outcome_timeout, winnerColor)
        }
        else -> stringResource(R.string.game_outcome_generic)
    }

    OverlayDialogWrapper(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.game_over),
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = message,
                style = Typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = boardTheme.lightColor,
                        contentColor = DeepDarkBackground
                    )
                ) {
                    Text(text = stringResource(R.string.dismiss))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNewGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = boardTheme.darkColor,
                        contentColor = DeepDarkBackground
                    )
                ) {
                    Text(text = stringResource(R.string.new_game))
                }
            }
        }
    }
}

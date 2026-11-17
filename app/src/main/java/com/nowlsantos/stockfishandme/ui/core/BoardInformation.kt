package com.nowlsantos.stockfishandme.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.presentation.GameViewModel
import com.nowlsantos.stockfishandme.ui.components.CapturedPieceSection
import com.nowlsantos.stockfishandme.ui.components.GameControlButtons
import com.nowlsantos.stockfishandme.ui.components.MoveHistoryLog
import com.nowlsantos.stockfishandme.ui.components.PlayerClock
import com.nowlsantos.stockfishandme.ui.theme.*
import com.nowlsantos.stockfishandme.utils.Utilities.formatMillis

@Composable
fun BoardInformation(
    uiState: GameState,
    perspective: PieceColor,
    isEngineThinking: Boolean,
    viewModel: GameViewModel,
    onReset: () -> Unit
) {
    val stockfishCapturedPieces = if (perspective == PieceColor.WHITE) uiState.whiteCapturedPieces else uiState.blackCapturedPieces
    val stockfishPieceColor = if (perspective == PieceColor.WHITE) PieceColor.WHITE else PieceColor.BLACK

    val userCapturedPieces = if (perspective == PieceColor.WHITE) uiState.blackCapturedPieces else uiState.whiteCapturedPieces
    val userPieceColor = if (perspective == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

    Column(modifier = Modifier.fillMaxSize()
    ){
        Column(modifier = Modifier.fillMaxWidth()
        ){
            /* Stockfish name and difficulty level */
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(uiState.boardTheme.darkColor),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically)
            {
                val stockfishText = stringResource(R.string.stockfish)
                Text(
                    text = if (isEngineThinking) "$stockfishText is thinking…" else stockfishText,
                    modifier = Modifier.padding(start = 16.dp),
                    color = White,
                    style = Typography.titleLarge
                )
                Text(
                    text = uiState.difficulty.displayName,
                    modifier = Modifier.padding(end = 16.dp),
                    color = White,
                    style = Typography.titleLarge
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(DarkGray),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically)
            {
                /* Captured pieces for Stockfish */
                CapturedPieceSection(
                    capturedPieces = stockfishCapturedPieces,
                    pieceColor = stockfishPieceColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Win: ${uiState.totalLosses} - Lost: ${uiState.totalWins}",
                    color = White,
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .background(DarkSurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val turnText = if (uiState.currentTurn == PieceColor.WHITE) {
                    stringResource(R.string.turn_white)
                } else {
                    stringResource(R.string.turn_black)
                }
                val activeTime = formatMillis(
                    if (uiState.currentTurn == PieceColor.WHITE) uiState.whiteTimeMillis else uiState.blackTimeMillis
                )

                Text(
                    text = "$turnText — $activeTime",
                    style = Typography.titleLarge,
                    color = if (uiState.currentTurn == PieceColor.WHITE) uiState.boardTheme.lightColor else uiState.boardTheme.darkColor
                )

                val whiteText = stringResource(R.string.piece_color_white)
                val blackText = stringResource(R.string.piece_color_black)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(
                            text = "$whiteText: ",
                            style = Typography.titleSmall,
                            color = uiState.boardTheme.lightColor
                        )
                        PlayerClock(
                            timeMillis = uiState.whiteTimeMillis,
                            isTurn = uiState.currentTurn == PieceColor.WHITE
                        )
                    }
                    Row {
                        Text(
                            text = "$blackText: ",
                            style = Typography.titleSmall,
                            color = uiState.boardTheme.darkColor
                        )
                        PlayerClock(
                            timeMillis = uiState.blackTimeMillis,
                            isTurn = uiState.currentTurn == PieceColor.BLACK
                        )
                    }
                }
            }
        }

        /* Central Move history logs */
        MoveHistoryLog(
            moveHistory = uiState.moveHistory,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        /* User and buttons */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(uiState.boardTheme.darkColor)
        ) {

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(DarkGray),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically)
            {
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = stringResource(R.string.you),
                    color = White,
                    style = Typography.titleLarge
                )

                /* Captured pieces for User */
                CapturedPieceSection(
                    capturedPieces = userCapturedPieces,
                    pieceColor = userPieceColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Win: ${uiState.totalWins} - Lost: ${uiState.totalLosses}",
                    color = White,
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                GameControlButtons(
                    onUndo = { viewModel.undoMove() },
                    onOfferDraw = { viewModel.offerDraw() },
                    onResign = { viewModel.requestResign() },
                    onReset = onReset
                )
            }

            // Bottom spacer filler
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(DeepDarkBackground)
            )
        }
    }
}

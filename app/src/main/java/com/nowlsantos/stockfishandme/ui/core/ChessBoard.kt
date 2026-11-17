package com.nowlsantos.stockfishandme.ui.core

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.GameLogic
import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType
import com.nowlsantos.stockfishandme.ui.components.SquareTile
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.White
import com.nowlsantos.stockfishandme.utils.PieceAssetProvider
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    uiState: GameState,
    perspective: PieceColor = PieceColor.WHITE,
    onTileClick: (Int, Int) -> Unit,
    onMakeMove: (Int, Int, Int, Int, Boolean) -> Unit,
    lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
) {
    val rows = 8
    val fileLabels = if (perspective == PieceColor.WHITE) {
        listOf("a", "b", "c", "d", "e", "f", "g", "h")
    } else {
        listOf("h", "g", "f", "e", "d", "c", "b", "a")
    }
    val rankLabels = if (perspective == PieceColor.WHITE) {
        (8 downTo 1).toList()
    } else {
        (1..8).toList()
    }

    val rankLabelWidth = 32.dp
    val fileLabelHeight = 28.dp

    var draggedPiece by remember { mutableStateOf<ChessPiece?>(null) }
    var dragSource by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var totalDragDelta by remember { mutableStateOf(Offset.Zero) }

    val currentUiState by rememberUpdatedState(uiState)
    val density = LocalDensity.current

    val effectiveLastMove = lastMove ?: uiState.lastMove
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(effectiveLastMove) {
        if (effectiveLastMove != null) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(200)
            )
        }
    }

    val isAnimating = animProgress.value < 1f && effectiveLastMove != null

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth - rankLabelWidth
        val availableHeight = maxHeight - fileLabelHeight
        val squareSize = maxOf(16.dp, minOf(availableWidth / rows, availableHeight / rows))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row {
                // Rank labels on left
                Column(
                    modifier = Modifier
                        .width(rankLabelWidth)
                        .height(squareSize * rows)
                        .background(DeepDarkBackground),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    rankLabels.forEach { rank ->
                        Text(
                            text = "$rank",
                            style = Typography.titleMedium,
                            color = White
                        )
                    }
                }

                // 8x8 Board Grid with floating drag & animation overlay container
                Box(
                    modifier = Modifier.size(squareSize * 8)
                ) {
                    Column {
                        val rowRange = if (perspective == PieceColor.WHITE) (0 until rows) else (rows - 1 downTo 0)
                        val colRange = if (perspective == PieceColor.WHITE) (0 until rows) else (rows - 1 downTo 0)

                        for (rowIndex in rowRange) {
                            Row {
                                for (colIndex in colRange) {
                                    val piece = uiState.board.getOrNull(rowIndex)?.getOrNull(colIndex)

                                    val isSelected = uiState.selectedSquare == Pair(rowIndex, colIndex)
                                    val isPossibleMove = Pair(rowIndex, colIndex) in uiState.legalMoves
                                    val isLastMoveSource = effectiveLastMove?.first == Pair(rowIndex, colIndex)
                                    val isLastMoveTarget = effectiveLastMove?.second == Pair(rowIndex, colIndex)

                                    val isKingInCheck = piece?.type == PieceType.KING &&
                                            piece.color == uiState.currentTurn &&
                                            (uiState.gameStatus == com.nowlsantos.stockfishandme.domain.GameStatus.CHECK ||
                                             uiState.gameStatus == com.nowlsantos.stockfishandme.domain.GameStatus.CHECKMATE)

                                    val isBeingDragged = dragSource == Pair(rowIndex, colIndex) && draggedPiece != null
                                    val isAnimatingTarget = isAnimating && effectiveLastMove.second == Pair(rowIndex, colIndex)

                                    SquareTile(
                                        size = squareSize,
                                        row = rowIndex,
                                        col = colIndex,
                                        piece = piece,
                                        isSelected = isSelected,
                                        isPossibleMove = isPossibleMove,
                                        isLastMoveSource = isLastMoveSource,
                                        isLastMoveTarget = isLastMoveTarget,
                                        isKingInCheck = isKingInCheck,
                                        boardTheme = uiState.boardTheme,
                                        onClick = { r, c ->
                                            val selected = uiState.selectedSquare
                                            if (selected == null) {
                                                onTileClick(r, c)
                                            } else if (selected == Pair(r, c)) {
                                                onTileClick(r, c)
                                            } else {
                                                val pieceAtSquare = uiState.board.getOrNull(r)?.getOrNull(c)
                                                if (pieceAtSquare != null && pieceAtSquare.color == uiState.currentTurn) {
                                                    onTileClick(r, c)
                                                } else {
                                                    val legalMoves = GameLogic.getLegalMoves(uiState, selected.first, selected.second)
                                                    if (Pair(r, c) in legalMoves) {
                                                        onMakeMove(selected.first, selected.second, r, c, false)
                                                    } else {
                                                        onTileClick(r, c)
                                                    }
                                                }
                                            }
                                        },
                                        onDragStart = { r, c ->
                                            val pieceAtSquare = currentUiState.board.getOrNull(r)?.getOrNull(c)
                                            if (pieceAtSquare != null && pieceAtSquare.color == currentUiState.currentTurn) {
                                                onTileClick(r, c)
                                                draggedPiece = pieceAtSquare
                                                dragSource = Pair(r, c)
                                                totalDragDelta = Offset.Zero
                                                dragOffset = Offset.Zero
                                            }
                                        },
                                        onDragUpdate = { dragAmount ->
                                            totalDragDelta += dragAmount
                                            dragOffset = totalDragDelta
                                        },
                                        onDragEnd = {
                                            val source = dragSource
                                            if (source != null && draggedPiece != null) {
                                                val (sourceRow, sourceCol) = source
                                                val squareSizePx = with(density) { squareSize.toPx() }
                                                val colDelta = (totalDragDelta.x / squareSizePx).roundToInt()
                                                val rowDelta = (totalDragDelta.y / squareSizePx).roundToInt()

                                                val targetCol = if (perspective == PieceColor.WHITE) {
                                                    sourceCol + colDelta
                                                } else {
                                                    sourceCol - colDelta
                                                }

                                                val targetRow = if (perspective == PieceColor.WHITE) {
                                                    sourceRow + rowDelta
                                                } else {
                                                    sourceRow - rowDelta
                                                }

                                                if (targetRow in 0..7 && targetCol in 0..7) {
                                                    val legalMoves = GameLogic.getLegalMoves(currentUiState, sourceRow, sourceCol)
                                                    if (Pair(targetRow, targetCol) in legalMoves) {
                                                        onMakeMove(sourceRow, sourceCol, targetRow, targetCol, true)
                                                    } else {
                                                        onTileClick(targetRow, targetCol)
                                                    }
                                                }
                                            }
                                            draggedPiece = null
                                            dragSource = null
                                            totalDragDelta = Offset.Zero
                                            dragOffset = Offset.Zero
                                        },
                                        isBeingDragged = isBeingDragged,
                                        isHiddenDuringAnimation = isAnimatingTarget
                                    )
                                }
                            }
                        }
                    }

                    // Floating slide animation overlay
                    if (isAnimating) {
                        val (from, to) = effectiveLastMove
                        val fromRow = from.first
                        val fromCol = from.second
                        val toRow = to.first
                        val toCol = to.second

                        val movingPiece = uiState.board.getOrNull(toRow)?.getOrNull(toCol)
                        if (movingPiece != null) {
                            val fromVisualRow = if (perspective == PieceColor.WHITE) fromRow else (7 - fromRow)
                            val fromVisualCol = if (perspective == PieceColor.WHITE) fromCol else (7 - fromCol)
                            val toVisualRow = if (perspective == PieceColor.WHITE) toRow else (7 - toRow)
                            val toVisualCol = if (perspective == PieceColor.WHITE) toCol else (7 - toCol)

                            val startX = squareSize * fromVisualCol
                            val startY = squareSize * fromVisualRow
                            val endX = squareSize * toVisualCol
                            val endY = squareSize * toVisualRow

                            val currentX = startX + (endX - startX) * animProgress.value
                            val currentY = startY + (endY - startY) * animProgress.value

                            val drawableId = PieceAssetProvider.getDrawableRes(movingPiece)

                            Box(
                                modifier = Modifier
                                    .size(squareSize)
                                    .offset(x = currentX, y = currentY)
                                    .zIndex(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = drawableId),
                                    contentDescription = null,
                                    modifier = Modifier.size(squareSize * 0.8f),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }

                    // Floating drag overlay
                    if (draggedPiece != null && dragSource != null) {
                        val (sourceRow, sourceCol) = dragSource!!
                        val visualRow = if (perspective == PieceColor.WHITE) sourceRow else (7 - sourceRow)
                        val visualCol = if (perspective == PieceColor.WHITE) sourceCol else (7 - sourceCol)

                        val x = (squareSize * visualCol) + with(density) { totalDragDelta.x.toDp() }
                        val y = (squareSize * visualRow) + with(density) { totalDragDelta.y.toDp() }

                        val piece = draggedPiece!!
                        val drawableId = PieceAssetProvider.getDrawableRes(piece)

                        Box(
                            modifier = Modifier
                                .size(squareSize)
                                .offset(x = x, y = y)
                                .zIndex(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = null,
                                modifier = Modifier.size(squareSize * 0.8f),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
            }

            // File labels at bottom
            Row(
                modifier = Modifier
                    .padding(start = rankLabelWidth)
                    .width(squareSize * rows)
                    .height(fileLabelHeight)
                    .background(DeepDarkBackground),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                fileLabels.forEach { label ->
                    Text(
                        text = label,
                        style = Typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = White
                    )
                }
            }
        }
    }
}

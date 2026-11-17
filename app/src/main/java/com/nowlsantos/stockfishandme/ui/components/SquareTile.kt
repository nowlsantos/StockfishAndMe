package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.ui.theme.*
import com.nowlsantos.stockfishandme.utils.PieceAssetProvider

@Composable
fun SquareTile(
    size: Dp,
    row: Int,
    col: Int,
    piece: ChessPiece?,
    isSelected: Boolean,
    isPossibleMove: Boolean,
    isLastMoveSource: Boolean,
    isLastMoveTarget: Boolean,
    isKingInCheck: Boolean,
    boardTheme: BoardTheme = BoardTheme.GREEN,
    onClick: (Int, Int) -> Unit,
    onDragStart: (Int, Int) -> Unit = { _, _ -> },
    onDragUpdate: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    isBeingDragged: Boolean = false,
    isHiddenDuringAnimation: Boolean = false
) {
    val isLightSquare = (row + col) % 2 == 0
    val defaultTileColor = if (isLightSquare) boardTheme.lightColor else boardTheme.darkColor
    val colorScheme = MaterialTheme.colorScheme

    val tileBackgroundColor = when {
        isLastMoveSource -> colorScheme.lastMoveSourceTile
        isLastMoveTarget -> colorScheme.lastMoveTargetTile
        isKingInCheck -> Color.Red.copy(alpha = 0.6f)
        isSelected -> colorScheme.selectedTile
        else -> defaultTileColor
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(tileBackgroundColor)
            .border(
                width = if (isKingInCheck) 3.dp else 0.dp,
                color = if (isKingInCheck) Color.Red else Color.Transparent
            )
            .pointerInput(row, col, piece) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downId = down.id
                        var isDragging = false
                        val slop = viewConfiguration.touchSlop
                        var accumulatedOffset = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == downId }
                            if (change == null) break

                            if (!change.pressed) {
                                if (!isDragging) {
                                    onClick(row, col)
                                } else {
                                    onDragEnd()
                                }
                                break
                            }

                            val delta = change.position - change.previousPosition
                            if (!isDragging) {
                                accumulatedOffset += delta
                                if (accumulatedOffset.getDistance() > slop) {
                                    isDragging = true
                                    onDragStart(row, col)
                                    onDragUpdate(accumulatedOffset)
                                    change.consume()
                                }
                            } else {
                                change.consume()
                                onDragUpdate(delta)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isBeingDragged && !isHiddenDuringAnimation) {
            piece?.let {
                val drawableId = PieceAssetProvider.getDrawableRes(it)
                val colorString = if (it.color == PieceColor.WHITE) stringResource(R.string.piece_color_white) else stringResource(R.string.piece_color_black)
                val description = "$colorString ${it.type.name}"
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = description,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(size * 0.8f)
                        .align(Alignment.Center)
                )
            }
        }

        if (isPossibleMove && !isSelected) {
            if (piece == null) {
                Box(
                    Modifier
                        .size(12.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(colorScheme.lastMoveSourceTile)
                )
            } else {
                Box(
                    Modifier
                        .size(size)
                        .align(Alignment.Center)
                        .border(
                            width = 3.dp,
                            color = colorScheme.lastMoveSourceTile,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

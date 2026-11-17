package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType
import com.nowlsantos.stockfishandme.ui.theme.Red
import com.nowlsantos.stockfishandme.ui.theme.White
import com.nowlsantos.stockfishandme.utils.PieceAssetProvider

@Composable
fun CapturedPieceSection(
    capturedPieces: List<ChessPiece>,
    pieceColor: PieceColor,
    modifier: Modifier = Modifier
) {
    val pieceTypes = listOf(
        PieceType.PAWN,
        PieceType.KNIGHT,
        PieceType.BISHOP,
        PieceType.ROOK,
        PieceType.QUEEN
    )

    val grouped = capturedPieces.groupBy { it.type }.mapValues { it.value.size }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pieceTypes.forEach { type ->
                    CapturedPieceSlot(type = type, count = grouped[type] ?: 0, color = pieceColor)
                }
            }
        }
    }
}

@Composable
fun CapturedPieceSlot(
    type: PieceType,
    count: Int,
    color: PieceColor
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(36.dp)
    ) {
        if (count > 0) {
            val drawableId = PieceAssetProvider.getDrawableRes(color, type)
            val colorString = if (color == PieceColor.WHITE) stringResource(R.string.piece_color_white) else stringResource(R.string.piece_color_black)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "$colorString ${type.name}",
                contentScale = ContentScale.Fit,
                //modifier = Modifier.size(35.dp)
            )
            if (count > 1) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 10.dp)
                        .background(Red, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        color = White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }
    }
}

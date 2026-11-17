package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.ui.theme.*

@Composable
fun EvaluationBar(
    evaluation: Float,
    perspective: PieceColor,
    boardTheme: BoardTheme,
    modifier: Modifier = Modifier
) {
    // evaluation: Positive means White advantage, Negative means Black advantage.
    // Range capped at -5 to +5 for visual representation.
    val cappedEval = evaluation.coerceIn(-5f, 5f)
    
    // Percentage of bar filled by White (0.0 to 1.0)
    // 0 -> completely black, 1 -> completely white
    // evaluation 0f should be 0.5f (50%)
    val whitePercentage = (cappedEval + 5f) / 10f
    
    // Reverse visual if playing as black (bottom is the player's side)
    // However, traditionally eval bar always has White at bottom.
    // Let's stick to standard: White is Bottom, Black is Top, or depending on perspective.
    // Actually, chess.com changes it based on perspective: player's color is at the bottom.
    // Let's implement that: Bottom is player's color.
    val bottomColorPercentage = if (perspective == PieceColor.WHITE) {
        whitePercentage
    } else {
        1f - whitePercentage
    }

    val topColorPercentage = 1f - bottomColorPercentage
    val bottomColor = if (perspective == PieceColor.WHITE) boardTheme.lightColor else boardTheme.darkColor
    val topColor = if (perspective == PieceColor.WHITE) boardTheme.darkColor else boardTheme.lightColor

    Box(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight() // 80% height of the board
            .background(Color.Gray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (topColorPercentage > 0) topColorPercentage else 0.001f)
                    .background(topColor)
            )
            // Bottom portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (bottomColorPercentage > 0) bottomColorPercentage else 0.001f)
                    .background(bottomColor)
            )
        }
    }
}

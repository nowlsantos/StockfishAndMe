package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.ui.theme.DarkGray
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.LightGray
import com.nowlsantos.stockfishandme.ui.theme.Turquoise
import com.nowlsantos.stockfishandme.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MoveHistoryLog(
    moveHistory: List<String>,
    modifier: Modifier = Modifier
) {
    val turns = remember(moveHistory) {
        val list = mutableListOf<ChessTurn>()
        for (i in moveHistory.indices step 2) {
            val moveNumber = (i / 2) + 1
            val whiteMove = moveHistory[i]
            val blackMove = if (i + 1 < moveHistory.size) moveHistory[i + 1] else null
            list.add(ChessTurn(moveNumber, whiteMove, blackMove))
        }
        list
    }

    val chunkedTurns = remember(turns) {
        turns.chunked(2)
    }

    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.background(DeepDarkBackground)
    ) {
        itemsIndexed(chunkedTurns) { rowIndex, turnPair ->
            val backgroundColor = if (rowIndex % 2 == 0) { DarkGray.copy(alpha = 0.5f)
            } else { DarkGray }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor,
                        //shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                turnPair.forEachIndexed { turnIndex, turn ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${turn.moveNumber}.",
                            style = Typography.bodyLarge,
                            color = Turquoise,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = turn.whiteMove,
                            style = Typography.bodyLarge,
                            color = LightGray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = turn.blackMove ?: "",
                            style = Typography.bodyLarge,
                            color = LightGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (turnIndex < turnPair.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                if (turnPair.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    LaunchedEffect(chunkedTurns.size) {
        if (chunkedTurns.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                listState.animateScrollToItem(chunkedTurns.size - 1)
            }
        }
    }
}

private data class ChessTurn(
    val moveNumber: Int,
    val whiteMove: String,
    val blackMove: String?
)

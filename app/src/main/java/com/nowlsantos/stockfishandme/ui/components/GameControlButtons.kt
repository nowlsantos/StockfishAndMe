package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.ui.theme.*

@Composable
fun GameControlButtons(
    onUndo: () -> Unit,
    onOfferDraw: () -> Unit,
    onResign: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onUndo,
            modifier = Modifier.weight(0.75f),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurfaceColor,
                contentColor = LightGray
            )
        ) {
            Text(
                text = stringResource(R.string.undo_move),
                style = Typography.labelLarge
            )
        }

        Button(
            onClick = onOfferDraw,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepPurple,
                contentColor = LightGray
            )
        ) {
            Text(
                text = stringResource(R.string.offer_draw),
                style = Typography.labelLarge
            )
        }

        Button(
            onClick = onResign,
            modifier = Modifier.weight(0.75f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Red.copy(alpha = 0.5f),
                contentColor = LightGray
            )
        ) {
            Text(
                text = stringResource(R.string.resign),
                style = Typography.labelLarge
            )
        }

        Button(
            onClick = onReset,
            modifier = Modifier.weight(0.75f),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepDarkGreen,
                contentColor = LightGray
            )
        ) {
            Text(
                text = stringResource(R.string.reset),
                style = Typography.labelLarge
            )
        }
    }
}

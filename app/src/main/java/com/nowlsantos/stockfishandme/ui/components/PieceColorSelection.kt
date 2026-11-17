package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.ui.theme.LightGray
import com.nowlsantos.stockfishandme.ui.theme.Transparent
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.White

@Composable
fun PieceColorSelection(
    selectedColor: PieceColor,
    onColorSelected: (PieceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val checked = selectedColor == PieceColor.WHITE
    Column(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_piece_color),
            style = Typography.labelLarge,
            color = LightGray
        )

        Row(
            modifier = modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.piece_color_black),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = White
            )

            Switch(
                modifier = Modifier.scale(1.4f),
                checked = checked,
                onCheckedChange = {
                    onColorSelected(if (it) PieceColor.WHITE else PieceColor.BLACK)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                    checkedTrackColor = MaterialTheme.colorScheme.surface,
                    checkedBorderColor = MaterialTheme.colorScheme.outline,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = Transparent,
                )
            )
            Text(
                text = stringResource(R.string.piece_color_white),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = White
            )
        }
    }
}

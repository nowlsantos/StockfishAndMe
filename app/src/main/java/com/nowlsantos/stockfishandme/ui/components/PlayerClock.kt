package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.nowlsantos.stockfishandme.ui.theme.LightGray
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.White
import com.nowlsantos.stockfishandme.utils.Utilities.formatMillis

@Composable
fun PlayerClock(
    timeMillis: Long,
    isTurn: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = Typography.titleSmall,
    activeColor: Color = White,
    inactiveColor: Color = LightGray
) {
    Text(
        text = formatMillis(timeMillis),
        style = style,
        color = if (isTurn) activeColor else inactiveColor,
        modifier = modifier
    )
}
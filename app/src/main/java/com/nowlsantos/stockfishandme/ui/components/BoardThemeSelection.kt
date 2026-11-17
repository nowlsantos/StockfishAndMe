package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.ui.theme.*

@Composable
fun BoardThemeSelection(
    selectedTheme: BoardTheme,
    onThemeSelected: (BoardTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_board_theme),
            style = Typography.labelLarge,
            color = LightGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoardTheme.entries.forEach { theme ->
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(theme.darkColor)
                        .semantics { contentDescription = theme.displayName }
                        .clickable { onThemeSelected(theme) }
                        .border(
                            width = if (theme == selectedTheme) 3.dp else 0.dp,
                            color = if (theme == selectedTheme) White else Transparent,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

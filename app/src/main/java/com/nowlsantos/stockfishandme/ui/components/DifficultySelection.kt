package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nowlsantos.stockfishandme.domain.Difficulty
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.ui.theme.DarkGray
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.LightGray
import com.nowlsantos.stockfishandme.ui.theme.Transparent
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.White
import com.nowlsantos.stockfishandme.ui.theme.darkColor

@Composable
fun DifficultySelection(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    difficultyOptions: List<Difficulty>,
    selectedTheme: BoardTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.select_chess_title),
            style = Typography.labelLarge,
            color = LightGray
        )

        difficultyOptions.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 30.dp)
                    .selectable(
                        selected = (item == selectedDifficulty),
                        onClick = { onDifficultySelected(item) },
                        role = Role.RadioButton
                    )
                    .clip(RoundedCornerShape(30))
                    .background(selectedTheme.darkColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier
                        .scale(1.1f)
                        .padding(start = 16.dp),
                    selected = (item == selectedDifficulty),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = White,
                        unselectedColor = MaterialTheme.colorScheme.outline,
                        disabledSelectedColor = MaterialTheme.colorScheme.outline,
                        disabledUnselectedColor = Transparent,
                    )
                )
                Text(
                    text = item.shortName,
                    fontWeight = FontWeight.Bold,
                    style = Typography.titleSmall,
                    color = White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

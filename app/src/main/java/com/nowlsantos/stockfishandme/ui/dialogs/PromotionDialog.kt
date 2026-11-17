package com.nowlsantos.stockfishandme.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType
import com.nowlsantos.stockfishandme.ui.components.OverlayDialogWrapper
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.utils.PieceAssetProvider

@Composable
fun PromotionDialog(
    color: PieceColor = PieceColor.WHITE,
    onPieceSelected: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val choices = listOf(
        PieceType.QUEEN to R.string.queen,
        PieceType.ROOK to R.string.rook,
        PieceType.BISHOP to R.string.bishop,
        PieceType.KNIGHT to R.string.knight
    )

    OverlayDialogWrapper(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.promotion_title),
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.promotion_choose_piece),
                style = Typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                choices.forEach { (type, labelResId) ->
                    val drawableId = PieceAssetProvider.getDrawableRes(color, type)

                    OutlinedButton(
                        onClick = { onPieceSelected(type) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = stringResource(labelResId),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = stringResource(labelResId),
                                style = Typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

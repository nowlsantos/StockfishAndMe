package com.nowlsantos.stockfishandme.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.ui.components.OverlayDialogWrapper
import com.nowlsantos.stockfishandme.ui.theme.DeepDarkBackground
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.darkColor

@Composable
fun PlayerResignDialog(
    onResign: () -> Unit,
    onCancel: () -> Unit,
    boardTheme: BoardTheme
) {
    OverlayDialogWrapper(onDismissRequest = onCancel) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.resign_confirm_title),
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.resign_confirm_message),
                style = Typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onResign,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = boardTheme.darkColor,
                        contentColor = DeepDarkBackground
                    )
                ) {
                    Text(text = stringResource(R.string.resign))
                }
            }
        }
    }
}

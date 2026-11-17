package com.nowlsantos.stockfishandme.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nowlsantos.stockfishandme.R

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nowlsantos.stockfishandme.ui.components.OverlayDialogWrapper
import com.nowlsantos.stockfishandme.ui.theme.Typography

@Composable
fun DrawDecisionDialog(
    messageRes: Int,
    onDismiss: () -> Unit
) {
    OverlayDialogWrapper(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(id = R.string.offer_draw_title),
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(id = messageRes),
                style = Typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.CenterEnd
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    }
}

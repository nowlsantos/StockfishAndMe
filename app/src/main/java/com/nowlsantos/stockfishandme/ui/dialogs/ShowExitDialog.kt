package com.nowlsantos.stockfishandme.ui.dialogs

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nowlsantos.stockfishandme.ui.theme.*

@Composable
fun ShowExitDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Black,
        textContentColor = White,
        title = {
            //SetDialogDim(dimAmount = 0.20f)
            Text(
                text = "Exit StockfishAndMe?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to close the game?",
                fontSize = 22.sp,
                color = White
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    (context as? Activity)?.finishAndRemoveTask()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = White
                )
            ) {
                Text(
                    text = "Exit",
                    fontSize = 18.sp
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = White
                )
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 18.sp
                )
            }
        }
    )
}
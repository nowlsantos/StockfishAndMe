package com.nowlsantos.stockfishandme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.ui.theme.Typography
import com.nowlsantos.stockfishandme.ui.theme.White

@Composable
fun CitationLink(
    onCitationClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefix = stringResource(R.string.citation_prefix)
    val engineName = stringResource(R.string.citation_engine_name)
    val suffix = stringResource(R.string.citation_suffix)
    val primaryColor = MaterialTheme.colorScheme.primary

    val citationText = buildAnnotatedString {
        append(prefix)
        pushStyle(
            SpanStyle(
                color = primaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        )
        append(engineName)
        pop()
        append(suffix)
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable { onCitationClicked() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = citationText,
            fontSize = 14.sp,
            style = Typography.bodyMedium,
            color = White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CitationToast(
    citationText: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 6.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = citationText,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.dismiss),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(4.dp)
                )
            }
        }
    }
}

package com.nowlsantos.stockfishandme.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.Difficulty
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.ui.components.BoardThemeSelection
import com.nowlsantos.stockfishandme.ui.components.CitationLink
import com.nowlsantos.stockfishandme.ui.components.CitationToast
import com.nowlsantos.stockfishandme.ui.components.DifficultySelection
import com.nowlsantos.stockfishandme.ui.components.PieceColorSelection
import com.nowlsantos.stockfishandme.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SelectionScreen(navController: NavController) {
    var selectedColor by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedDepth by remember { mutableIntStateOf(10) }
    var selectedTheme by remember { mutableStateOf(BoardTheme.GREEN) }

    val titleOptions = Difficulty.entries
    val (selectedDifficulty, onDifficultySelected) = remember { mutableStateOf(Difficulty.NATIONAL_MASTER) }

    var showCitationToast by remember { mutableStateOf(false) }
    val citationDetails = stringResource(R.string.citation_details)
    val coroutineScope = rememberCoroutineScope()

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chess_bg),
            contentDescription = stringResource(R.string.cd_chess_background),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.4f
        )

        Box(
            modifier = Modifier
                .width(500.dp)
                .fillMaxHeight()
                .background(DeepDarkBackground.copy(alpha = 0.3f)),
                //.background(LightGray.copy(alpha = 0.1f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 16.dp)
                    .zIndex(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                //Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.title_app_stockfish_ai_me),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = White
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Color Selection
                PieceColorSelection(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f), color = LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Level Selection
                DifficultySelection(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = onDifficultySelected,
                    difficultyOptions = titleOptions,
                    selectedTheme = selectedTheme
                )

                selectedDepth = selectedDifficulty.depth

                Spacer(modifier = Modifier.height(16.dp))
                
                // Theme Selection
                BoardThemeSelection(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            "chessScreen/${selectedColor}/$selectedDepth/${selectedTheme.name}"
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White,
                        containerColor = selectedTheme.darkColor,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = stringResource(R.string.start_game),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                //Spacer(modifier = Modifier.height(16.dp))
                CitationLink(
                    onCitationClicked = {
                        showCitationToast = true
                        coroutineScope.launch {
                            delay(5000.milliseconds)
                            showCitationToast = false
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (showCitationToast) {
                CitationToast(
                    citationText = citationDetails,
                    isVisible = true,
                    onDismiss = { showCitationToast = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .zIndex(2f)
                )
            }
        }
    }
}

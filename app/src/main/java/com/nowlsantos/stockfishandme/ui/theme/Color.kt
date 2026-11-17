package com.nowlsantos.stockfishandme.ui.theme

import androidx.compose.ui.graphics.Color
import com.nowlsantos.stockfishandme.domain.BoardTheme

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF3E3E3E)
val Pink40 = Color(0xFF323232)

val Transparent = Color.Transparent
val White = Color.White
val Black = Color.Black
val DarkGray = Color(0xFF393939)
val LightGray = Color(0xFFD3D3D3)
val DeepPurple = Color(0xFF3F2A7F)
val DeepDarkBackground = Color(0xFF121212)    // Custom dark background
val DarkSurfaceColor = Color(0xFF1E1E1E)
val Red = Color.Red
val DeepDarkGreen = Color(0xFF064C0C)
val Gold = Color(0xFFFFD700)
val HighLight = Color(0x80FFFF00)
val Orange = Color(0xFFFFA500)
val DarkGreen = Color(0xFF547D2B)
val LightGreen = Color(0xFFEEEED2)
val LightBrown = Color(0xFFfcddb1)
val DarkBrown = Color(0xFFa07147)
val Turquoise = Color(0xFF37a9b4)
val DarkMaroon = Color(0xFF7e242e)

val BoardTheme.darkColor: Color
    get() = when (this) {
        BoardTheme.GREEN -> DarkGreen
        BoardTheme.BROWN -> DarkBrown
        BoardTheme.TURQUOISE -> Turquoise
        BoardTheme.MAROON -> DarkMaroon
    }

val BoardTheme.lightColor: Color
    get() = when (this) {
        BoardTheme.GREEN -> LightGreen
        BoardTheme.BROWN -> LightBrown
        BoardTheme.TURQUOISE -> White
        BoardTheme.MAROON -> White
    }

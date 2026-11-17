package com.nowlsantos.stockfishandme.utils

import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType

object PieceAssetProvider {
    fun getDrawableRes(piece: ChessPiece): Int {
        return when (piece.color) {
            PieceColor.WHITE -> when (piece.type) {
                PieceType.PAWN -> R.drawable.w_pawn
                PieceType.KNIGHT -> R.drawable.w_knight
                PieceType.BISHOP -> R.drawable.w_bishop
                PieceType.ROOK -> R.drawable.w_rook
                PieceType.QUEEN -> R.drawable.w_queen
                PieceType.KING -> R.drawable.w_king
            }
            PieceColor.BLACK -> when (piece.type) {
                PieceType.PAWN -> R.drawable.b_pawn
                PieceType.KNIGHT -> R.drawable.b_knight
                PieceType.BISHOP -> R.drawable.b_bishop
                PieceType.ROOK -> R.drawable.b_rook
                PieceType.QUEEN -> R.drawable.b_queen
                PieceType.KING -> R.drawable.b_king
            }
        }
    }

    fun getDrawableRes(color: PieceColor, type: PieceType): Int {
        return getDrawableRes(ChessPiece(type, color))
    }
}

package com.nowlsantos.stockfishandme.utils

import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.PieceColor

object Utilities {
    // Converts square coordinates (row, col) to UCI string (e.g. 6, 4 -> "e2")
    fun squareToUci(row: Int, col: Int): String {
        val fileChar = ('a' + col)
        val rankChar = ('8' - row)
        return "$fileChar$rankChar"
    }

    // Converts move (fromRow, fromCol) to (toRow, toCol) to UCI move string (e.g. "e2e4")
    fun moveToUci(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, promotion: Char? = null): String {
        val from = squareToUci(fromRow, fromCol)
        val to = squareToUci(toRow, toCol)
        return "$from$to${promotion ?: ""}"
    }

    // Parses GameState to FEN string
    fun boardToFen(state: GameState): String {
        return boardToFen(
            board = state.board,
            turn = state.currentTurn,
            whiteKingside = state.whiteCastlingKingside,
            whiteQueenside = state.whiteCastlingQueenside,
            blackKingside = state.blackCastlingKingside,
            blackQueenside = state.blackCastlingQueenside,
            enPassantTarget = state.enPassantTarget,
            halfmove = state.halfMoveClock,
            fullmove = state.fullMoveNumber
        )
    }

    // Parses board to FEN string with detailed parameters
    fun boardToFen(
        board: List<List<ChessPiece?>>,
        turn: PieceColor,
        whiteKingside: Boolean = true,
        whiteQueenside: Boolean = true,
        blackKingside: Boolean = true,
        blackQueenside: Boolean = true,
        enPassantTarget: Pair<Int, Int>? = null,
        halfmove: Int = 0,
        fullmove: Int = 1
    ): String {
        val fenBuilder = StringBuilder()
        for (r in 0..7) {
            var emptyCount = 0
            for (c in 0..7) {
                val piece = board.getOrNull(r)?.getOrNull(c)
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        fenBuilder.append(emptyCount)
                        emptyCount = 0
                    }
                    fenBuilder.append(piece.toFenChar())
                }
            }
            if (emptyCount > 0) {
                fenBuilder.append(emptyCount)
            }
            if (r < 7) {
                fenBuilder.append('/')
            }
        }

        val turnChar = if (turn == PieceColor.WHITE) "w" else "b"

        val castlingSb = StringBuilder()
        if (whiteKingside) castlingSb.append('K')
        if (whiteQueenside) castlingSb.append('Q')
        if (blackKingside) castlingSb.append('k')
        if (blackQueenside) castlingSb.append('q')
        val castlingStr = if (castlingSb.isEmpty()) "-" else castlingSb.toString()

        val epStr = if (enPassantTarget != null) {
            squareToUci(enPassantTarget.first, enPassantTarget.second)
        } else {
            "-"
        }

        fenBuilder.append(" $turnChar $castlingStr $epStr $halfmove $fullmove")
        return fenBuilder.toString()
    }

    fun formatMillis(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}

package com.nowlsantos.stockfishandme.domain

enum class BoardTheme(val displayName: String) {
    GREEN("Classic Green"),
    BROWN("Wood Brown"),
    TURQUOISE("Ocean Turquoise"),
    MAROON("Royal Maroon")
}

enum class PieceType(val fenChar: Char) {
    PAWN('p'),
    KNIGHT('n'),
    BISHOP('b'),
    ROOK('r'),
    QUEEN('q'),
    KING('k')
}

enum class PieceColor {
    WHITE, BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class GameStatus {
    ACTIVE,
    CHECK,
    CHECKMATE,
    DRAW,
    RESIGNED,
    TIMEOUT;

    val isOngoing: Boolean
        get() = this == ACTIVE || this == CHECK

    val isGameOver: Boolean
        get() = !isOngoing
}

enum class Difficulty(
    val depth: Int,
    val skillLevel: Int,
    val elo: Int?,
    val displayName: String,
    val shortName: String
) {
    NATIONAL_MASTER(12, 16, 2000, "National Master", "National Master (NM)"),
    CANDIDATE_MASTER(14, 17, 2200, "Candidate Master", "Candidate Master (CM)"),
    FIDE_MASTER(16, 18, 2300, "FIDE Master", "FIDE Master (FM)"),
    INTERNATIONAL_MASTER(18, 19, 2450, "International Master", "International Master (IM)"),
    GRANDMASTER(20, 20, 2700, "Grandmaster", "Grandmaster (GM)");
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor
) {
    fun toFenChar(): Char {
        val char = type.fenChar
        return if (color == PieceColor.WHITE) char.uppercaseChar() else char
    }
}

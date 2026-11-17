package com.nowlsantos.stockfishandme.domain

data class GameState(
    val board: List<List<ChessPiece?>> = createInitialBoard(),
    val currentTurn: PieceColor = PieceColor.WHITE,
    val playerColor: PieceColor = PieceColor.WHITE,
    val gameStatus: GameStatus = GameStatus.ACTIVE,
    val activeDialog: DialogType? = null,
    val difficulty: Difficulty = Difficulty.NATIONAL_MASTER,
    val boardTheme: BoardTheme = BoardTheme.GREEN,
    val selectedSquare: Pair<Int, Int>? = null,
    val legalMoves: List<Pair<Int, Int>> = emptyList(),
    val moveHistory: List<String> = emptyList(),
    val whiteCapturedPieces: List<ChessPiece> = emptyList(),
    val blackCapturedPieces: List<ChessPiece> = emptyList(),
    val whiteTimeMillis: Long = 600_000L,
    val blackTimeMillis: Long = 600_000L,
    val isEngineThinking: Boolean = false,
    val pendingPromotionMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val resignedPlayer: PieceColor? = null,
    val toastMessageRes: Int? = null,
    val lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val fen: String = INITIAL_FEN,
    val evaluation: Float = 0f,
    val whiteCastlingKingside: Boolean = true,
    val whiteCastlingQueenside: Boolean = true,
    val blackCastlingKingside: Boolean = true,
    val blackCastlingQueenside: Boolean = true,
    val enPassantTarget: Pair<Int, Int>? = null,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
    val drawDecisionMessageRes: Int? = null,
    val historyStack: List<GameState> = emptyList()
) {
    companion object {
        const val INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        fun createInitialBoard(): List<List<ChessPiece?>> {
            val board = mutableListOf<List<ChessPiece?>>()
            board.add(
                listOf(
                    ChessPiece(PieceType.ROOK, PieceColor.BLACK),
                    ChessPiece(PieceType.KNIGHT, PieceColor.BLACK),
                    ChessPiece(PieceType.BISHOP, PieceColor.BLACK),
                    ChessPiece(PieceType.QUEEN, PieceColor.BLACK),
                    ChessPiece(PieceType.KING, PieceColor.BLACK),
                    ChessPiece(PieceType.BISHOP, PieceColor.BLACK),
                    ChessPiece(PieceType.KNIGHT, PieceColor.BLACK),
                    ChessPiece(PieceType.ROOK, PieceColor.BLACK)
                )
            )
            board.add(List(8) { ChessPiece(PieceType.PAWN, PieceColor.BLACK) })
            repeat(4) {
                board.add(List(8) { null })
            }
            board.add(List(8) { ChessPiece(PieceType.PAWN, PieceColor.WHITE) })
            board.add(
                listOf(
                    ChessPiece(PieceType.ROOK, PieceColor.WHITE),
                    ChessPiece(PieceType.KNIGHT, PieceColor.WHITE),
                    ChessPiece(PieceType.BISHOP, PieceColor.WHITE),
                    ChessPiece(PieceType.QUEEN, PieceColor.WHITE),
                    ChessPiece(PieceType.KING, PieceColor.WHITE),
                    ChessPiece(PieceType.BISHOP, PieceColor.WHITE),
                    ChessPiece(PieceType.KNIGHT, PieceColor.WHITE),
                    ChessPiece(PieceType.ROOK, PieceColor.WHITE)
                )
            )
            return board
        }
    }
}

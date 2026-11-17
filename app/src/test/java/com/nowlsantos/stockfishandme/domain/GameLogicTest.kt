package com.nowlsantos.stockfishandme.domain

import com.nowlsantos.stockfishandme.utils.Utilities
import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    @Test
    fun initialFenIsCorrect() {
        val state = GameState()
        val fen = Utilities.boardToFen(state)
        assertEquals(GameState.INITIAL_FEN, fen)
    }

    @Test
    fun testCastlingKingsideWhite() {
        // Position where White can castle Kingside (squares f1, g1 empty)
        val initialBoard = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        initialBoard[7][5] = null // f1
        initialBoard[7][6] = null // g1

        val state = GameState(board = initialBoard, currentTurn = PieceColor.WHITE)
        val kingMoves = GameLogic.getLegalMoves(state, 7, 4)

        // Square (7, 6) is g1 (O-O)
        assertTrue(kingMoves.contains(Pair(7, 6)))

        // Apply castling move
        val nextState = GameLogic.applyMove(state, 7, 4, 7, 6)
        // King on g1, Rook on f1
        assertEquals(ChessPiece(PieceType.KING, PieceColor.WHITE), nextState.board[7][6])
        assertEquals(ChessPiece(PieceType.ROOK, PieceColor.WHITE), nextState.board[7][5])
        assertNull(nextState.board[7][4])
        assertNull(nextState.board[7][7])
        assertFalse(nextState.whiteCastlingKingside)
        assertFalse(nextState.whiteCastlingQueenside)
    }

    @Test
    fun testCastlingBlockedWhenAttacked() {
        // Position where White wants to castle Kingside, but f1 is attacked by Black Rook on f8
        val initialBoard = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        initialBoard[7][5] = null // f1 empty
        initialBoard[7][6] = null // g1 empty
        initialBoard[6][5] = null // remove f2 pawn so f-file is open
        initialBoard[1][5] = null // remove f7 pawn
        initialBoard[0][5] = ChessPiece(PieceType.ROOK, PieceColor.BLACK) // Black Rook on f8

        val state = GameState(board = initialBoard, currentTurn = PieceColor.WHITE)
        val kingMoves = GameLogic.getLegalMoves(state, 7, 4)

        // Square (7, 6) must NOT be legal because f1 is under attack
        assertFalse(kingMoves.contains(Pair(7, 6)))
    }

    @Test
    fun testEnPassantWhite() {
        // Setup: White pawn on e5 (3, 4), Black advances pawn f7 -> f5 (1, 5 to 3, 5)
        val board = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        board[3][4] = ChessPiece(PieceType.PAWN, PieceColor.WHITE) // e5
        board[6][4] = null // remove original e2 pawn

        val state = GameState(board = board, currentTurn = PieceColor.BLACK)
        // Black plays f7 -> f5 (from row 1, col 5 to row 3, col 5)
        val stateAfterBlackMove = GameLogic.applyMove(state, 1, 5, 3, 5)

        assertEquals(Pair(2, 5), stateAfterBlackMove.enPassantTarget) // e.p. target is f6 (2, 5)

        // White pawn on e5 (3, 4) should have legal move to f6 (2, 5)
        val whitePawnMoves = GameLogic.getLegalMoves(stateAfterBlackMove, 3, 4)
        assertTrue(whitePawnMoves.contains(Pair(2, 5)))

        // Apply en passant capture
        val stateAfterCapture = GameLogic.applyMove(stateAfterBlackMove, 3, 4, 2, 5)
        assertNull(stateAfterCapture.board[3][5]) // Black pawn on f5 removed
        assertEquals(ChessPiece(PieceType.PAWN, PieceColor.WHITE), stateAfterCapture.board[2][5]) // White pawn on f6
        assertEquals(1, stateAfterCapture.blackCapturedPieces.size)
    }

    @Test
    fun testKingInCheckFiltersIllegalMove() {
        // Setup: White King on e1 (7, 4), White Rook on e2 (6, 4), Black Rook on e8 (0, 4)
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[6][4] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
        board[0][4] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        // White Rook on e2 is pinned to King. Moving off file e (e.g. to d2 (6, 3)) must be illegal.
        val rookMoves = GameLogic.getLegalMoves(state, 6, 4)
        assertFalse(rookMoves.contains(Pair(6, 3)))
        // Moving along file e (e.g. e3, e4, e5, e6, e7, e8) keeps King safe
        assertTrue(rookMoves.contains(Pair(5, 4)))
    }

    @Test
    fun testPawnPromotion() {
        // Setup: White Pawn on e7 (1, 4) promotes to e8 (0, 4) as Queen
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[7][0] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[0][7] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[1][4] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        val promotedState = GameLogic.applyMove(state, 1, 4, 0, 4, promotionType = PieceType.QUEEN)

        assertEquals(ChessPiece(PieceType.QUEEN, PieceColor.WHITE), promotedState.board[0][4])
        assertNull(promotedState.board[1][4])
    }

    @Test
    fun testKingCanMoveWhenInCheck() {
        // Setup: White King on e1 (7, 4), Black Rook on e8 (0, 4) giving check along file e.
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[0][4] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        board[0][7] = ChessPiece(PieceType.KING, PieceColor.BLACK)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        assertTrue(GameLogic.isKingInCheck(state.board, PieceColor.WHITE))

        val kingMoves = GameLogic.getLegalMoves(state, 7, 4)
        // Moving to d1 (7, 3) or f1 (7, 5) steps off file e and escapes check
        assertTrue(kingMoves.contains(Pair(7, 3)))
        assertTrue(kingMoves.contains(Pair(7, 5)))
        // Moving to e2 (6, 4) stays on file e and remains in check, so it must NOT be legal
        assertFalse(kingMoves.contains(Pair(6, 4)))

        // Executing the legal escape move (7, 4) -> (7, 3)
        val nextState = GameLogic.applyMove(state, 7, 4, 7, 3)
        assertEquals(ChessPiece(PieceType.KING, PieceColor.WHITE), nextState.board[7][3])
        assertNull(nextState.board[7][4])
        assertFalse(GameLogic.isKingInCheck(nextState.board, PieceColor.WHITE))
    }

    @Test
    fun testKingCanCaptureAttackingPieceToGetOutOfCheck() {
        // Setup: White King on e1 (7, 4), Black Queen on e2 (6, 4) checking White King directly.
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[6][4] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
        board[0][7] = ChessPiece(PieceType.KING, PieceColor.BLACK)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        assertTrue(GameLogic.isKingInCheck(state.board, PieceColor.WHITE))

        val kingMoves = GameLogic.getLegalMoves(state, 7, 4)
        // King can capture undefended Queen on e2 (6, 4)
        assertTrue(kingMoves.contains(Pair(6, 4)))

        val nextState = GameLogic.applyMove(state, 7, 4, 6, 4)
        assertEquals(ChessPiece(PieceType.KING, PieceColor.WHITE), nextState.board[6][4])
        assertNull(nextState.board[7][4])
        assertFalse(GameLogic.isKingInCheck(nextState.board, PieceColor.WHITE))
    }

    @Test
    fun testEnPassantTargetResetOnNormalMove() {
        val board = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        val state = GameState(board = board, currentTurn = PieceColor.WHITE)

        // White double step e2 -> e4 sets enPassantTarget to e3 (5, 4)
        val state1 = GameLogic.applyMove(state, 6, 4, 4, 4)
        assertEquals(Pair(5, 4), state1.enPassantTarget)

        // Black plays knight b8 -> c6 (0, 1 -> 2, 2)
        val state2 = GameLogic.applyMove(state1, 0, 1, 2, 2)
        // Non-double-pawn move must reset enPassantTarget to null
        assertNull(state2.enPassantTarget)
    }

    @Test
    fun testCheckmateDetection() {
        // Fool's Mate setup: 1. f3 e5 2. g4 Qh4#
        val board = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        val state0 = GameState(board = board, currentTurn = PieceColor.WHITE)
        val state1 = GameLogic.applyMove(state0, 6, 5, 5, 5) // f3
        val state2 = GameLogic.applyMove(state1, 1, 4, 3, 4) // e5
        val state3 = GameLogic.applyMove(state2, 6, 6, 4, 6) // g4
        val state4 = GameLogic.applyMove(state3, 0, 3, 4, 7) // Qh4#

        assertEquals(GameStatus.CHECKMATE, state4.gameStatus)
    }

    @Test
    fun testStalemateDetection() {
        // Setup stalemate position: Black King on a8 (0, 0), White King on c7 (1, 2), White Queen moves b5 (3, 1) to b6 (2, 1)
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[0][0] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[1][2] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[3][1] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        // White plays Queen b5 (3, 1) -> b6 (2, 1). Next turn is Black, who is not in check but has no legal moves -> Stalemate (DRAW)
        val applied = GameLogic.applyMove(state, 3, 1, 2, 1, checkGameStatus = true)
        assertEquals(GameStatus.DRAW, applied.gameStatus)
    }

    @Test
    fun testFiftyMoveRule() {
        val board = List(8) { MutableList<ChessPiece?>(8) { null } }
        board[0][0] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[7][7] = ChessPiece(PieceType.KING, PieceColor.WHITE)

        val state = GameState(board = board, currentTurn = PieceColor.WHITE, halfMoveClock = 99)
        val nextState = GameLogic.applyMove(state, 7, 7, 6, 7)
        assertEquals(100, nextState.halfMoveClock)
        assertEquals(GameStatus.DRAW, nextState.gameStatus)
    }

    @Test
    fun testApplyMoveIsDragSetsLastMoveNull() {
        val board = GameState.createInitialBoard()
        val state = GameState(board = board, currentTurn = PieceColor.WHITE)
        val normalState = GameLogic.applyMove(state, 6, 4, 4, 4, isDrag = false)
        assertNotNull(normalState.lastMove)

        val dragBoard = GameState.createInitialBoard()
        val dragStartState = GameState(board = dragBoard, currentTurn = PieceColor.WHITE)
        val dragApplyState = GameLogic.applyMove(dragStartState, 6, 4, 4, 4, isDrag = true)
        assertNull(dragApplyState.lastMove)
    }
}

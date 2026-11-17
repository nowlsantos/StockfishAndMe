package com.nowlsantos.stockfishandme.domain

import com.nowlsantos.stockfishandme.utils.Utilities
import kotlin.math.abs

object
GameLogic {

    // Computes legal moves for a piece at (row, col) using full GameState context
    fun getLegalMoves(state: GameState, row: Int, col: Int): List<Pair<Int, Int>> {
        val piece = state.board.getOrNull(row)?.getOrNull(col) ?: return emptyList()
        if (piece.color != state.currentTurn) return emptyList()

        val pseudoLegalMoves = getPseudoLegalMoves(state, row, col)
        val legalMoves = mutableListOf<Pair<Int, Int>>()

        for (move in pseudoLegalMoves) {
            val targetRow = move.first
            val targetCol = move.second

            // Simulate move on state without recursive game status checks
            val simulatedState = applyMoveInternal(state, row, col, targetRow, targetCol)
            if (!isKingInCheck(simulatedState.board, piece.color)) {
                legalMoves.add(move)
            }
        }

        return legalMoves
    }

    // Generates pseudo-legal moves before filtering for King safety
    private fun getPseudoLegalMoves(state: GameState, row: Int, col: Int): List<Pair<Int, Int>> {
        val board = state.board
        val piece = board.getOrNull(row)?.getOrNull(col) ?: return emptyList()
        val pseudoLegal = mutableListOf<Pair<Int, Int>>()

        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1

                // Forward step
                val forwardRow = row + direction
                if (forwardRow in 0..7 && board[forwardRow][col] == null) {
                    pseudoLegal.add(Pair(forwardRow, col))
                    // Double step from starting rank
                    val doubleRow = row + (direction * 2)
                    if (row == startRow && doubleRow in 0..7 && board[doubleRow][col] == null) {
                        pseudoLegal.add(Pair(doubleRow, col))
                    }
                }

                // Standard captures
                for (dCol in listOf(-1, 1)) {
                    val capCol = col + dCol
                    val capRow = row + direction
                    if (capRow in 0..7 && capCol in 0..7) {
                        val target = board[capRow][capCol]
                        if (target != null && target.color != piece.color) {
                            pseudoLegal.add(Pair(capRow, capCol))
                        }
                    }
                }

                // En Passant capture
                val epTarget = state.enPassantTarget
                if (epTarget != null) {
                    val epRow = epTarget.first
                    val epCol = epTarget.second
                    if (epRow == row + direction && abs(col - epCol) == 1) {
                        pseudoLegal.add(Pair(epRow, epCol))
                    }
                }
            }
            PieceType.KNIGHT -> {
                val knightMoves = listOf(
                    Pair(-2, -1), Pair(-2, 1),
                    Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2),
                    Pair(2, -1), Pair(2, 1)
                )
                for ((first, second) in knightMoves) {
                    val r = row + first
                    val c = col + second
                    if (r in 0..7 && c in 0..7) {
                        val target = board[r][c]
                        if (target == null || target.color != piece.color) {
                            pseudoLegal.add(Pair(r, c))
                        }
                    }
                }
            }
            PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN -> {
                val directions = mutableListOf<Pair<Int, Int>>()
                if (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
                }
                if (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
                }

                for ((first, second) in directions) {
                    var r = row + first
                    var c = col + second
                    while (r in 0..7 && c in 0..7) {
                        val target = board[r][c]
                        if (target == null) {
                            pseudoLegal.add(Pair(r, c))
                        } else {
                            if (target.color != piece.color) {
                                pseudoLegal.add(Pair(r, c))
                            }
                            break
                        }
                        r += first
                        c += second
                    }
                }
            }
            PieceType.KING -> {
                val kingMoves = listOf(
                    Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
                    Pair(0, -1), Pair(0, 1),
                    Pair(1, -1), Pair(1, 0), Pair(1, 1)
                )
                for ((first, second) in kingMoves) {
                    val r = row + first
                    val c = col + second
                    if (r in 0..7 && c in 0..7) {
                        val target = board[r][c]
                        if (target == null || target.color != piece.color) {
                            pseudoLegal.add(Pair(r, c))
                        }
                    }
                }

                // Castling moves
                val opponentColor = piece.color.opposite()
                if (piece.color == PieceColor.WHITE && row == 7 && col == 4) {
                    // White Kingside Castling (O-O)
                    if (state.whiteCastlingKingside &&
                        board[7][5] == null && board[7][6] == null &&
                        board[7][7]?.type == PieceType.ROOK && board[7][7]?.color == PieceColor.WHITE &&
                        !isSquareAttacked(board, Pair(7, 4), opponentColor) &&
                        !isSquareAttacked(board, Pair(7, 5), opponentColor) &&
                        !isSquareAttacked(board, Pair(7, 6), opponentColor)
                    ) {
                        pseudoLegal.add(Pair(7, 6))
                    }
                    // White Queenside Castling (O-O-O)
                    if (state.whiteCastlingQueenside &&
                        board[7][1] == null && board[7][2] == null && board[7][3] == null &&
                        board[7][0]?.type == PieceType.ROOK && board[7][0]?.color == PieceColor.WHITE &&
                        !isSquareAttacked(board, Pair(7, 4), opponentColor) &&
                        !isSquareAttacked(board, Pair(7, 3), opponentColor) &&
                        !isSquareAttacked(board, Pair(7, 2), opponentColor)
                    ) {
                        pseudoLegal.add(Pair(7, 2))
                    }
                } else if (piece.color == PieceColor.BLACK && row == 0 && col == 4) {
                    // Black Kingside Castling (O-O)
                    if (state.blackCastlingKingside &&
                        board[0][5] == null && board[0][6] == null &&
                        board[0][7]?.type == PieceType.ROOK && board[0][7]?.color == PieceColor.BLACK &&
                        !isSquareAttacked(board, Pair(0, 4), opponentColor) &&
                        !isSquareAttacked(board, Pair(0, 5), opponentColor) &&
                        !isSquareAttacked(board, Pair(0, 6), opponentColor)
                    ) {
                        pseudoLegal.add(Pair(0, 6))
                    }
                    // Black Queenside Castling (O-O-O)
                    if (state.blackCastlingQueenside &&
                        board[0][1] == null && board[0][2] == null && board[0][3] == null &&
                        board[0][0]?.type == PieceType.ROOK && board[0][0]?.color == PieceColor.BLACK &&
                        !isSquareAttacked(board, Pair(0, 4), opponentColor) &&
                        !isSquareAttacked(board, Pair(0, 3), opponentColor) &&
                        !isSquareAttacked(board, Pair(0, 2), opponentColor)
                    ) {
                        pseudoLegal.add(Pair(0, 2))
                    }
                }
            }
        }
        return pseudoLegal
    }

    // Checks if square is attacked by any piece of attackerColor
    fun isSquareAttacked(board: List<List<ChessPiece?>>, square: Pair<Int, Int>, attackerColor: PieceColor): Boolean {
        val targetRow = square.first
        val targetCol = square.second

        // Pawns
        val pawnDir = if (attackerColor == PieceColor.WHITE) 1 else -1 // white attacks upward (row - 1 from target)
        val pawnRow = targetRow + pawnDir
        if (pawnRow in 0..7) {
            for (pawnCol in listOf(targetCol - 1, targetCol + 1)) {
                if (pawnCol in 0..7) {
                    val piece = board[pawnRow][pawnCol]
                    if (piece != null && piece.color == attackerColor && piece.type == PieceType.PAWN) {
                        return true
                    }
                }
            }
        }

        // Knights
        val knightOffsets = listOf(
            Pair(-2, -1), Pair(-2, 1),
            Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2),
            Pair(2, -1), Pair(2, 1)
        )
        for ((first, second) in knightOffsets) {
            val r = targetRow + first
            val c = targetCol + second
            if (r in 0..7 && c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == attackerColor && piece.type == PieceType.KNIGHT) {
                    return true
                }
            }
        }

        // King
        val kingOffsets = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1), Pair(0, 1),
            Pair(1, -1), Pair(1, 0), Pair(1, 1)
        )
        for ((first, second) in kingOffsets) {
            val r = targetRow + first
            val c = targetCol + second
            if (r in 0..7 && c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == attackerColor && piece.type == PieceType.KING) {
                    return true
                }
            }
        }

        // Diagonal sliders (Bishop, Queen)
        val diagonalDirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        for ((first, second) in diagonalDirs) {
            var r = targetRow + first
            var c = targetCol + second
            while (r in 0..7 && c in 0..7) {
                val piece = board[r][c]
                if (piece != null) {
                    if (piece.color == attackerColor && (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += first
                c += second
            }
        }

        // Orthogonal sliders (Rook, Queen)
        val orthogonalDirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        for ((first, second) in orthogonalDirs) {
            var r = targetRow + first
            var c = targetCol + second
            while (r in 0..7 && c in 0..7) {
                val piece = board[r][c]
                if (piece != null) {
                    if (piece.color == attackerColor && (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                r += first
                c += second
            }
        }

        return false
    }

    // Checks if the King of color is currently in check on board
    fun isKingInCheck(board: List<List<ChessPiece?>>, color: PieceColor): Boolean {
        var kingSquare: Pair<Int, Int>? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == color && piece.type == PieceType.KING) {
                    kingSquare = Pair(r, c)
                    break
                }
            }
            if (kingSquare != null) break
        }
        return kingSquare != null && isSquareAttacked(board, kingSquare, color.opposite())
    }

    // Internal move application for board simulation
    private fun applyMoveInternal(state: GameState, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): GameState {
        return applyMove(state, fromRow, fromCol, toRow, toCol, promotionType = null, checkGameStatus = false, isDrag = false)
    }

    // Applies move to GameState and returns updated GameState
    fun applyMove(
        state: GameState,
        fromRow: Int,
        fromCol: Int,
        toRow: Int,
        toCol: Int,
        promotionType: PieceType? = null,
        checkGameStatus: Boolean = true,
        isDrag: Boolean = false
    ): GameState {
        val board = state.board.map { it.toMutableList() }.toMutableList()
        val piece = board[fromRow][fromCol] ?: return state
        val target = board[toRow][toCol]

        var newWhiteCastlingKingside = state.whiteCastlingKingside
        var newWhiteCastlingQueenside = state.whiteCastlingQueenside
        var newBlackCastlingKingside = state.blackCastlingKingside
        var newBlackCastlingQueenside = state.blackCastlingQueenside

        var newEnPassantTarget: Pair<Int, Int>? = null
        var capturedPiece: ChessPiece? = target

        // Handle Castling Rook movements
        if (piece.type == PieceType.KING && abs(toCol - fromCol) == 2) {
            if (piece.color == PieceColor.WHITE) {
                newWhiteCastlingKingside = false
                newWhiteCastlingQueenside = false
                if (toCol == 6) { // O-O
                    board[7][7] = null
                    board[7][5] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
                } else if (toCol == 2) { // O-O-O
                    board[7][0] = null
                    board[7][3] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
                }
            } else {
                newBlackCastlingKingside = false
                newBlackCastlingQueenside = false
                if (toCol == 6) { // O-O
                    board[0][7] = null
                    board[0][5] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
                } else if (toCol == 2) { // O-O-O
                    board[0][0] = null
                    board[0][3] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
                }
            }
        }

        // Handle En Passant Capture
        if (piece.type == PieceType.PAWN && target == null && fromCol != toCol) {
            if (state.enPassantTarget == Pair(toRow, toCol)) {
                capturedPiece = board[fromRow][toCol]
                board[fromRow][toCol] = null
            }
        }

        // Update En Passant Target for double pawn step
        if (piece.type == PieceType.PAWN && abs(toRow - fromRow) == 2) {
            val epRow = (fromRow + toRow) / 2
            newEnPassantTarget = Pair(epRow, fromCol)
        }

        // Update Castling Rights when King or Rook moves or Rook is captured
        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE) {
                newWhiteCastlingKingside = false
                newWhiteCastlingQueenside = false
            } else {
                newBlackCastlingKingside = false
                newBlackCastlingQueenside = false
            }
        }

        if (fromRow == 7 && fromCol == 7) newWhiteCastlingKingside = false
        if (fromRow == 7 && fromCol == 0) newWhiteCastlingQueenside = false
        if (fromRow == 0 && fromCol == 7) newBlackCastlingKingside = false
        if (fromRow == 0 && fromCol == 0) newBlackCastlingQueenside = false

        if (toRow == 7 && toCol == 7) newWhiteCastlingKingside = false
        if (toRow == 7 && toCol == 0) newWhiteCastlingQueenside = false
        if (toRow == 0 && toCol == 7) newBlackCastlingKingside = false
        if (toRow == 0 && toCol == 0) newBlackCastlingQueenside = false

        // Move piece
        board[fromRow][fromCol] = null
        val movedPiece = if (piece.type == PieceType.PAWN && (toRow == 0 || toRow == 7) && promotionType != null) {
            ChessPiece(promotionType, piece.color)
        } else {
            piece
        }
        board[toRow][toCol] = movedPiece

        // Handle Captured Pieces
        val newWhiteCaptured = state.whiteCapturedPieces.toMutableList()
        val newBlackCaptured = state.blackCapturedPieces.toMutableList()
        if (capturedPiece != null) {
            if (capturedPiece.color == PieceColor.WHITE) {
                newWhiteCaptured.add(capturedPiece)
            } else {
                newBlackCaptured.add(capturedPiece)
            }
        }

        // Update Halfmove clock and Fullmove number
        val isPawnMove = piece.type == PieceType.PAWN
        val isCapture = capturedPiece != null
        val newHalfMoveClock = if (isPawnMove || isCapture) 0 else state.halfMoveClock + 1
        val newFullMoveNumber = if (state.currentTurn == PieceColor.BLACK) state.fullMoveNumber + 1 else state.fullMoveNumber

        val nextTurn = state.currentTurn.opposite()

        val promotionChar = if (movedPiece.type != piece.type) {
            when (movedPiece.type) {
                PieceType.QUEEN -> 'q'
                PieceType.ROOK -> 'r'
                PieceType.BISHOP -> 'b'
                PieceType.KNIGHT -> 'n'
                else -> null
            }
        } else null

        val uciMove = Utilities.moveToUci(fromRow, fromCol, toRow, toCol, promotionChar)
        val newHistory = state.moveHistory + uciMove

        val tempStateForStatus = state.copy(
            board = board,
            currentTurn = nextTurn,
            whiteCastlingKingside = newWhiteCastlingKingside,
            whiteCastlingQueenside = newWhiteCastlingQueenside,
            blackCastlingKingside = newBlackCastlingKingside,
            blackCastlingQueenside = newBlackCastlingQueenside,
            enPassantTarget = newEnPassantTarget,
            halfMoveClock = newHalfMoveClock,
            fullMoveNumber = newFullMoveNumber
        )

        val newFen = Utilities.boardToFen(tempStateForStatus)

        val status = if (checkGameStatus) {
            val hasLegalMoves = playerHasLegalMoves(tempStateForStatus, nextTurn)
            val inCheck = isKingInCheck(board, nextTurn)
            when {
                !hasLegalMoves && inCheck -> GameStatus.CHECKMATE
                !hasLegalMoves && !inCheck -> GameStatus.DRAW // Stalemate
                newHalfMoveClock >= 100 -> GameStatus.DRAW // 50-move rule
                inCheck -> GameStatus.CHECK
                else -> GameStatus.ACTIVE
            }
        } else {
            GameStatus.ACTIVE
        }

        val newLastMove = if (isDrag) null else Pair(Pair(fromRow, fromCol), Pair(toRow, toCol))

        return tempStateForStatus.copy(
            gameStatus = status,
            moveHistory = newHistory,
            whiteCapturedPieces = newWhiteCaptured,
            blackCapturedPieces = newBlackCaptured,
            fen = newFen,
            selectedSquare = null,
            legalMoves = emptyList(),
            lastMove = newLastMove
        )
    }

    private fun playerHasLegalMoves(state: GameState, color: PieceColor): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = state.board[r][c]
                if (piece != null && piece.color == color) {
                    val moves = getLegalMoves(state, r, c)
                    if (moves.isNotEmpty()) return true
                }
            }
        }
        return false
    }
}

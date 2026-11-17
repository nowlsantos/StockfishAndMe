package com.nowlsantos.stockfishandme.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nowlsantos.stockfishandme.R
import com.nowlsantos.stockfishandme.domain.BoardTheme
import com.nowlsantos.stockfishandme.domain.DialogType
import com.nowlsantos.stockfishandme.domain.Difficulty
import com.nowlsantos.stockfishandme.domain.GameLogic
import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.domain.GameStatus
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType
import com.nowlsantos.stockfishandme.engine.StockfishEngine
import com.nowlsantos.stockfishandme.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val stockfishEngine: StockfishEngine,
    private val soundManager: SoundManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState>(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var playerColor: PieceColor = PieceColor.WHITE
    private var timerJob: Job? = null
    private var engineJob: Job? = null

    val isEngineThinking: StateFlow<Boolean> = _gameState
        .map { it.isEngineThinking }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            var firstEmission = true
            stockfishEngine.evaluationFlow.collect { eval ->
                if (firstEmission) {
                    firstEmission = false
                    if (_gameState.value.evaluation != 0f) return@collect
                }
                _gameState.update { state ->
                    state.copy(evaluation = eval)
                }
            }
        }
    }

    fun startNewGame(
        playerColor: PieceColor = PieceColor.WHITE,
        difficulty: Difficulty = Difficulty.NATIONAL_MASTER,
        boardTheme: BoardTheme = BoardTheme.GREEN
    ) {
        this.playerColor = playerColor
        engineJob?.cancel()
        timerJob?.cancel()

        _gameState.update { currentState ->
            GameState(
                board = GameState.createInitialBoard(),
                currentTurn = PieceColor.WHITE,
                playerColor = playerColor,
                difficulty = difficulty,
                boardTheme = boardTheme,
                gameStatus = GameStatus.ACTIVE,
                totalWins = currentState.totalWins,
                totalLosses = currentState.totalLosses
            )
        }

        engineJob = viewModelScope.launch {
            stockfishEngine.stopEngine()
            stockfishEngine.startEngine()
            stockfishEngine.setDifficulty(difficulty)
            stockfishEngine.newGame()

            if (playerColor == PieceColor.BLACK) {
                triggerEngineMoveInternal()
            }
        }
    }

    fun resetGame() {
        startNewGame(
            playerColor = playerColor,
            difficulty = _gameState.value.difficulty,
            boardTheme = _gameState.value.boardTheme
        )
    }

    fun onSquareClicked(row: Int, col: Int) {
        val state = _gameState.value
        if (state.gameStatus.isGameOver) return
        if (state.isEngineThinking) return

        val board = state.board
        val selected = state.selectedSquare

        if (selected == null) {
            val piece = board.getOrNull(row)?.getOrNull(col)
            if (piece != null && piece.color == state.currentTurn) {
                val legalMoves = GameLogic.getLegalMoves(state, row, col)
                _gameState.update {
                    it.copy(
                        selectedSquare = Pair(row, col),
                        legalMoves = legalMoves
                    )
                }
            }
        } else {
            if (selected == Pair(row, col)) {
                // Deselect
                _gameState.update {
                    it.copy(
                        selectedSquare = null,
                        legalMoves = emptyList()
                    )
                }
            } else if (Pair(row, col) in state.legalMoves) {
                val fromRow = selected.first
                val fromCol = selected.second
                makeMove(fromRow, fromCol, row, col)
            } else {
                val piece = board.getOrNull(row)?.getOrNull(col)
                if (piece != null && piece.color == state.currentTurn) {
                    val legalMoves = GameLogic.getLegalMoves(state, row, col)
                    _gameState.update {
                        it.copy(
                            selectedSquare = Pair(row, col),
                            legalMoves = legalMoves
                        )
                    }
                } else {
                    _gameState.update {
                        it.copy(
                            selectedSquare = null,
                            legalMoves = emptyList()
                        )
                    }
                }
            }
        }
    }

    fun makeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, isDrag: Boolean = false): Boolean {
        val state = _gameState.value
        if (state.gameStatus.isGameOver) return false

        val piece = state.board.getOrNull(fromRow)?.getOrNull(fromCol) ?: return false
        val legalMoves = GameLogic.getLegalMoves(state, fromRow, fromCol)
        if (Pair(toRow, toCol) !in legalMoves) return false

        // Check for promotion
        if (piece.type == PieceType.PAWN && (toRow == 0 || toRow == 7)) {
            _gameState.update {
                it.copy(
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    activeDialog = DialogType.PROMOTION,
                    pendingPromotionMove = Pair(Pair(fromRow, fromCol), Pair(toRow, toCol)),
                    gameStatus = GameStatus.ACTIVE
                )
            }
            return true
        }

        executeMove(fromRow, fromCol, toRow, toCol, null, isDrag)
        return true
    }

    private fun executeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, promotionType: PieceType?, isDrag: Boolean) {
        var isCheck = false
        var isCapture = false
        _gameState.update { currentState ->
            val target = currentState.board.getOrNull(toRow)?.getOrNull(toCol)
            isCapture = target != null
            val newHistory = currentState.historyStack + currentState
            val newState = GameLogic.applyMove(
                state = currentState,
                fromRow = fromRow,
                fromCol = fromCol,
                toRow = toRow,
                toCol = toCol,
                promotionType = promotionType,
                checkGameStatus = true,
                isDrag = isDrag
            ).copy(historyStack = newHistory)

            isCheck = newState.gameStatus == GameStatus.CHECK || newState.gameStatus == GameStatus.CHECKMATE

            val isOver = newState.gameStatus.isGameOver
            val activeDialog = if (isOver) DialogType.GAME_OVER else null
            val newTotalWins = if (isOver && newState.gameStatus == GameStatus.CHECKMATE && newState.currentTurn != playerColor) currentState.totalWins + 1 else currentState.totalWins
            val newTotalLosses = if (isOver && newState.gameStatus == GameStatus.CHECKMATE && newState.currentTurn == playerColor) currentState.totalLosses + 1 else currentState.totalLosses

            newState.copy(
                activeDialog = activeDialog,
                totalWins = newTotalWins,
                totalLosses = newTotalLosses
            )
        }

        when {
            isCheck -> soundManager?.playCheckSound()
            isCapture -> soundManager?.playCaptureSound()
            else -> soundManager?.playMoveSound()
        }

        val updatedState = _gameState.value
        if (!updatedState.gameStatus.isGameOver && updatedState.currentTurn != playerColor) {
            triggerEngineMove()
        }
    }

    fun promotePawn(pieceType: PieceType) {
        val state = _gameState.value
        val promoMove = state.pendingPromotionMove
        if (promoMove != null) {
            val from = promoMove.first
            val to = promoMove.second
            _gameState.update { it.copy(activeDialog = null, pendingPromotionMove = null) }
            executeMove(from.first, from.second, to.first, to.second, pieceType, false)
        }
    }

    private fun triggerEngineMove() {
        engineJob?.cancel()
        engineJob = viewModelScope.launch {
            triggerEngineMoveInternal()
        }
    }

    private suspend fun triggerEngineMoveInternal() {
        _gameState.update { it.copy(isEngineThinking = true) }
        val state = _gameState.value
        val fen = state.fen

        val result = stockfishEngine.findBestMove(fen, state.difficulty.depth)
        val bestMoveStr = result.getOrNull()

        if (bestMoveStr != null && bestMoveStr.length >= 4) {
            val fromCol = bestMoveStr[0] - 'a'
            val fromRow = 8 - (bestMoveStr[1].digitToIntOrNull() ?: 1)
            val toCol = bestMoveStr[2] - 'a'
            val toRow = 8 - (bestMoveStr[3].digitToIntOrNull() ?: 1)
            val promotionChar = if (bestMoveStr.length >= 5) bestMoveStr[4] else null
            val promotionType = when (promotionChar) {
                'q' -> PieceType.QUEEN
                'r' -> PieceType.ROOK
                'b' -> PieceType.BISHOP
                'n' -> PieceType.KNIGHT
                else -> null
            }

            _gameState.update { it.copy(isEngineThinking = false) }
            executeMove(fromRow, fromCol, toRow, toCol, promotionType, false)
        } else {
            // Fallback move generation if engine fails
            Log.w("GameViewModel", "Stockfish engine failed or returned invalid move ($bestMoveStr). Triggering fallback heuristic move generator.")
            val fallback = generateFallbackMove(state)
            _gameState.update { it.copy(isEngineThinking = false) }
            if (fallback != null) {
                val (from, to, promo) = fallback
                executeMove(from.first, from.second, to.first, to.second, promo, false)
            }
        }
    }

    fun generateFallbackMove(state: GameState): Triple<Pair<Int, Int>, Pair<Int, Int>, PieceType?>? {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = state.board[r][c]
                if (piece != null && piece.color == state.currentTurn) {
                    val legalMoves = GameLogic.getLegalMoves(state, r, c)
                    if (legalMoves.isNotEmpty()) {
                        val target = legalMoves.first()
                        val promo = if (piece.type == PieceType.PAWN && (target.first == 0 || target.first == 7)) PieceType.QUEEN else null
                        return Triple(Pair(r, c), target, promo)
                    }
                }
            }
        }
        return null
    }

    fun requestResign() {
        _gameState.update {
            it.copy(activeDialog = DialogType.RESIGN)
        }
    }

    fun confirmResign() {
        val state = _gameState.value
        _gameState.update {
            it.copy(
                gameStatus = GameStatus.RESIGNED,
                resignedPlayer = state.currentTurn,
                activeDialog = DialogType.GAME_OVER,
                totalLosses = it.totalLosses + 1
            )
        }
    }

    fun cancelResign() {
        _gameState.update { it.copy(activeDialog = null) }
    }

    fun dismissDialogs() {
        _gameState.update { it.copy(activeDialog = null) }
    }

    fun offerDraw() {
        val state = _gameState.value
        if (state.moveHistory.size < 10) {
            _gameState.update {
                it.copy(
                    toastMessageRes = R.string.stockfish_declined_draw
                )
            }
            return
        }

        val eval = state.evaluation
        // If evaluation is favorable to AI (> 0.8), AI declines draw
        val aiIsWhite = playerColor == PieceColor.BLACK
        val aiAdvantage = if (aiIsWhite) eval else -eval

        if (aiAdvantage > 0.8f) {
            _gameState.update {
                it.copy(
                    activeDialog = DialogType.DRAW_DECISION,
                    drawDecisionMessageRes = R.string.draw_offer_declined
                )
            }
        } else {
            _gameState.update {
                it.copy(
                    gameStatus = GameStatus.DRAW,
                    activeDialog = DialogType.GAME_OVER,
                    drawDecisionMessageRes = R.string.draw_offer_accepted
                )
            }
        }
    }

    fun undoMove() {
        val state = _gameState.value
        if (state.isEngineThinking || state.gameStatus.isGameOver) return

        val stepsToUndo = if (state.historyStack.size >= 2) 2 else if (state.historyStack.size == 1) 1 else 0
        if (stepsToUndo > 0) {
            val targetState = state.historyStack[state.historyStack.size - stepsToUndo]
            val newHistory = state.historyStack.dropLast(stepsToUndo)
            _gameState.update {
                targetState.copy(
                    historyStack = newHistory,
                    activeDialog = null,
                    toastMessageRes = null
                )
            }
            soundManager?.playMoveSound()
            engineJob?.cancel()
        }
    }

    fun finalizeDrawDecision() {
        _gameState.update { it.copy(activeDialog = null, drawDecisionMessageRes = null) }
    }

    fun clearToastMessage() {
        _gameState.update { it.copy(toastMessageRes = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stockfishEngine.stopEngine()
    }
}

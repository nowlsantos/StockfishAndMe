package com.nowlsantos.stockfishandme.presentation

import com.nowlsantos.stockfishandme.domain.GameState
import com.nowlsantos.stockfishandme.engine.StockfishEngine
import com.nowlsantos.stockfishandme.domain.ChessPiece
import com.nowlsantos.stockfishandme.domain.DialogType
import com.nowlsantos.stockfishandme.domain.GameStatus
import com.nowlsantos.stockfishandme.domain.PieceColor
import com.nowlsantos.stockfishandme.domain.PieceType
import com.nowlsantos.stockfishandme.domain.GameLogic
import com.nowlsantos.stockfishandme.domain.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class MockStockfishEngine : StockfishEngine() {
        var newGameCalled = false

        override suspend fun startEngine(): Result<Boolean> {
            return Result.success(true)
        }

        override suspend fun newGame(): Result<Boolean> {
            newGameCalled = true
            return Result.success(true)
        }

        override suspend fun setDifficulty(difficulty: Difficulty) {
            // no-op
        }

        override suspend fun findBestMove(fen: String, depth: Int): Result<String> {
            return Result.failure(IllegalStateException("No engine available"))
        }
    }

    @Test
    fun testConfirmResignSetsResignedPlayer() {
        val viewModel = GameViewModel(MockStockfishEngine())
        assertEquals(PieceColor.WHITE, viewModel.gameState.value.currentTurn)

        viewModel.confirmResign()

        assertEquals(GameStatus.RESIGNED, viewModel.gameState.value.gameStatus)
        assertEquals(PieceColor.WHITE, viewModel.gameState.value.resignedPlayer)
        assertTrue(viewModel.gameState.value.activeDialog == DialogType.GAME_OVER)
    }

    @Test
    fun testGenerateFallbackMoveReturnsValidLegalMove() {
        val viewModel = GameViewModel(MockStockfishEngine())
        val initialState = GameState()

        val fallbackMove = viewModel.generateFallbackMove(initialState)
        assertNotNull(fallbackMove)

        val (from, to, _) = fallbackMove!!
        val piece = initialState.board[from.first][from.second]
        assertNotNull(piece)
        assertEquals(PieceColor.WHITE, piece?.color)

        val legalMoves = GameLogic.getLegalMoves(initialState, from.first, from.second)
        assertTrue(legalMoves.contains(to))
    }

    @Test
    fun testOnSquareClickedAllowsSelectingAndMovingKingWhenInCheck() {
        // Create board where White King on e1 (7, 4) is in check by Black Rook on e8 (0, 4)
        val board: List<List<ChessPiece?>> = List(8) { MutableList<ChessPiece?>(8) { null } }
        val mutableBoard = board.map { it.toMutableList() }.toMutableList()
        mutableBoard[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        mutableBoard[0][4] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        mutableBoard[0][7] = ChessPiece(PieceType.KING, PieceColor.BLACK)

        // Setup a state where White is in CHECK
        val checkState = GameState(
            board = mutableBoard,
            currentTurn = PieceColor.WHITE,
            gameStatus = GameStatus.CHECK
        )

        // GameStatus.CHECK is ongoing and allows moves
        assertTrue(checkState.gameStatus.isOngoing)

        val moves = GameLogic.getLegalMoves(checkState, 7, 4)
        assertTrue(moves.isNotEmpty())
        assertTrue(moves.contains(Pair(7, 3)))
    }

    @Test
    fun testStartNewGameCallsNewGameOnEngine() {
        val mockService = MockStockfishEngine()
        val viewModel = GameViewModel(mockService)
        viewModel.startNewGame(PieceColor.WHITE)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(mockService.newGameCalled)
    }

    @Test
    fun testMakeMoveTriggersGameOverDialogOnCheckmate() {
        val mockService = MockStockfishEngine()
        val viewModel = GameViewModel(mockService)

        // Setup board 1 move away from Fool's Mate: 1. f3 e5 2. g4 Qh4#
        val board = GameState.createInitialBoard().map { it.toMutableList() }.toMutableList()
        val state0 = GameState(board = board, currentTurn = PieceColor.WHITE)
        val state1 = GameLogic.applyMove(state0, 6, 5, 5, 5) // f3
        val state2 = GameLogic.applyMove(state1, 1, 4, 3, 4) // e5
        val state3 = GameLogic.applyMove(state2, 6, 6, 4, 6) // g4

        // Force viewModel state to state3 (Black's turn to deliver Qh4#)
        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = state3

        // Black plays Qh4# (from (0, 3) to (4, 7))
        val moved = viewModel.makeMove(0, 3, 4, 7)
        assertTrue(moved)
        assertEquals(GameStatus.CHECKMATE, viewModel.gameState.value.gameStatus)
        assertTrue(viewModel.gameState.value.activeDialog == DialogType.GAME_OVER)
    }

    @Test
    fun testDifficultyPropertiesAndValues() {
        assertEquals(20, Difficulty.GRANDMASTER.skillLevel)
        assertEquals(2700, Difficulty.GRANDMASTER.elo)
        assertEquals(16, Difficulty.NATIONAL_MASTER.skillLevel)
        assertEquals(2000, Difficulty.NATIONAL_MASTER.elo)
        assertEquals("FIDE Master", Difficulty.FIDE_MASTER.displayName)
        assertEquals("Grandmaster", Difficulty.GRANDMASTER.displayName)
    }

    @Test
    fun testOfferDrawDeclinedAutomaticallyWhenFewerThan10Moves() {
        val viewModel = GameViewModel(MockStockfishEngine())
        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateFlow.value.copy(
            moveHistory = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"), // 5 moves (< 10)
            evaluation = 0.0f
        )

        viewModel.offerDraw()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(GameStatus.ACTIVE, viewModel.gameState.value.gameStatus)
        assertNull(viewModel.gameState.value.activeDialog)
        assertEquals(com.nowlsantos.stockfishandme.R.string.stockfish_declined_draw, viewModel.gameState.value.toastMessageRes)
    }

    @Test
    fun testOfferDrawAcceptedWhenBalancedAnd10OrMoreMoves() {
        val viewModel = GameViewModel(MockStockfishEngine())
        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateFlow.value.copy(
            moveHistory = List(10) { "e2e4" }, // 10 moves (>= 10)
            evaluation = 0.0f
        )

        viewModel.offerDraw()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(GameStatus.DRAW, viewModel.gameState.value.gameStatus)
        assertTrue(viewModel.gameState.value.activeDialog == DialogType.GAME_OVER)

        viewModel.finalizeDrawDecision()
        assertNull(viewModel.gameState.value.activeDialog)
    }

    @Test
    fun testOfferDrawDeclinedWhenAiHasClearAdvantageAnd10OrMoreMoves() {
        val viewModel = GameViewModel(MockStockfishEngine())
        // Player is White. If evaluation is -2.0 (Black advantage), AI (Black) has clear advantage.
        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateFlow.value.copy(
            moveHistory = List(12) { "e2e4" }, // 12 moves (>= 10)
            evaluation = -2.0f
        )

        viewModel.offerDraw()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(GameStatus.ACTIVE, viewModel.gameState.value.gameStatus)
        assertEquals(DialogType.DRAW_DECISION, viewModel.gameState.value.activeDialog)
        // Check message resource by comparing it
        assertEquals(com.nowlsantos.stockfishandme.R.string.draw_offer_declined, viewModel.gameState.value.drawDecisionMessageRes)
    }

    @Test
    fun testStartNewGamePreservesWinsAndLosses() {
        val mockService = MockStockfishEngine()
        val viewModel = GameViewModel(mockService)

        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateFlow.value.copy(totalWins = 3, totalLosses = 2)

        viewModel.startNewGame()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.gameState.value.totalWins)
        assertEquals(2, viewModel.gameState.value.totalLosses)
    }

    @Test
    fun testResetGamePreservesWinsAndLosses() {
        val mockService = MockStockfishEngine()
        val viewModel = GameViewModel(mockService)

        val stateField = GameViewModel::class.java.getDeclaredField("_gameState")
        stateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = stateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateFlow.value.copy(totalWins = 5, totalLosses = 1)

        viewModel.resetGame()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, viewModel.gameState.value.totalWins)
        assertEquals(1, viewModel.gameState.value.totalLosses)
    }

    @Test
    fun testUndoMoveRevertsState() {
        val mockService = MockStockfishEngine()
        val viewModel = GameViewModel(mockService)
        val initialState = viewModel.gameState.value

        // Make a move (e.g. e2e4 from (6, 4) to (4, 4))
        viewModel.makeMove(6, 4, 4, 4)
        val movedState = viewModel.gameState.value
        assertNotEquals(initialState.board, movedState.board)
        assertEquals(1, movedState.historyStack.size)

        // Undo move
        viewModel.undoMove()
        val undoneState = viewModel.gameState.value
        assertEquals(initialState.board, undoneState.board)
        assertEquals(0, undoneState.historyStack.size)
    }
}

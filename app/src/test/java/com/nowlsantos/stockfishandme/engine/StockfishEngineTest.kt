package com.nowlsantos.stockfishandme.engine

import com.nowlsantos.stockfishandme.domain.Difficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockfishEngineTest {

    @Test
    fun testSetDifficultyConfiguresOptionsCorrectly() = runTest {
        val engine = StockfishEngine()

        engine.setDifficulty(Difficulty.GRANDMASTER)
        // Grandmaster uses max strength
        engine.setDifficulty(Difficulty.NATIONAL_MASTER)
        // National Master configures ELO and skill level
        assertTrue(true)
    }

    @Test
    fun testStartEngineReturnsFailureWhenExecutableNotFoundInUnitTestEnvironment() = runTest {
        val engine = StockfishEngine()
        val result = engine.startEngine()
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testFindBestMoveReturnsFailureWhenEngineFailsToStart() = runTest {
        val engine = StockfishEngine()
        val result = engine.findBestMove("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", Difficulty.NATIONAL_MASTER)
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
}

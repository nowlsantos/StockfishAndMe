package com.nowlsantos.stockfishandme.engine

import android.content.Context
import android.util.Log
import com.nowlsantos.stockfishandme.domain.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

open class StockfishEngine(private val context: Context? = null) {
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val isInitialized = AtomicBoolean(false)
    private val mutex = Mutex()
    private val engineOptions = mutableMapOf<String, String>()

    val evaluationFlow = MutableStateFlow(0f)

    @Suppress("ObsoleteSdkInt")
    private fun getStockfishExecutable(): Result<String> {
        try {
            // 1. Try executable directly in nativeLibraryDir (standard location for extracted native binaries)
            val nativeDir = context?.applicationInfo?.nativeLibraryDir
            if (nativeDir != null) {
                val candidateNames = listOf("libstockfish.so", "stockfish", "libstockfish_exec.so")
                for (name in candidateNames) {
                    val libFile = File(nativeDir, name)
                    if (libFile.exists()) {
                        libFile.setExecutable(true, false)
                        if (libFile.canExecute()) {
                            Log.d(TAG, "Using Stockfish executable directly from nativeLibraryDir: ${libFile.absolutePath}")
                            return Result.success(libFile.absolutePath)
                        }
                    }
                }
            }

            // 2. Fallback check: copy to codeCacheDir or filesDir if not in nativeLibraryDir
            val execDir = context?.codeCacheDir ?: context?.filesDir
            val execFile = if (execDir != null) File(execDir, "stockfish_exec") else null

            if (execFile != null) {
                try {
                    if (nativeDir != null) {
                        val candidateNames = listOf("libstockfish.so", "stockfish")
                        for (name in candidateNames) {
                            val libFile = File(nativeDir, name)
                            if (libFile.exists()) {
                                try {
                                    libFile.inputStream().use { input ->
                                        execFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    Log.d(TAG, "Copied $name from nativeLibraryDir to stockfish_exec: ${execFile.absolutePath}")
                                    break
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to copy $name from nativeLibraryDir", e)
                                }
                            }
                        }
                    }

                    execFile.setExecutable(true, false)
                    if (execFile.exists() && execFile.canExecute()) {
                        Log.d(TAG, "Stockfish executable ready at: ${execFile.absolutePath}")
                        return Result.success(execFile.absolutePath)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to prepare stockfish_exec file", e)
                }

                // Fallback check if execFile already exists and is executable
                if (execFile.exists()) {
                    execFile.setExecutable(true, false)
                    if (execFile.canExecute()) {
                        Log.d(TAG, "Using existing executable stockfish_exec at: ${execFile.absolutePath}")
                        return Result.success(execFile.absolutePath)
                    }
                }
            }

            // 3. Fallback check system PATH
            val systemPaths = listOf("/system/bin/stockfish", "/system/xbin/stockfish")
            for (path in systemPaths) {
                val systemPathFile = File(path)
                if (systemPathFile.exists()) {
                    systemPathFile.setExecutable(true, false)
                    if (systemPathFile.canExecute()) {
                        Log.d(TAG, "Found Stockfish executable at system PATH: ${systemPathFile.absolutePath}")
                        return Result.success(systemPathFile.absolutePath)
                    }
                }
            }

            val errorMsg = "Stockfish native executable not found or could not be made executable in nativeLibraryDir, codeCacheDir, filesDir, or system PATH"
            Log.e(TAG, errorMsg)
            return Result.failure(IllegalStateException(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving Stockfish executable path", e)
            return Result.failure(e)
        }
    }

    open suspend fun startEngine(): Result<Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            performStartEngineInternal()
        }
    }

    private suspend fun performStartEngineInternal(): Result<Boolean> {
        if (isInitialized.get() && process != null && process?.isAlive == true) {
            return Result.success(true)
        }

        performStopEngineInternal()

        val execResult = getStockfishExecutable()
        if (execResult.isFailure) {
            val err = execResult.exceptionOrNull() ?: IllegalStateException("Stockfish executable not found")
            Log.e(TAG, "Cannot start Stockfish engine: ${err.message}")
            return Result.failure(err)
        }

        val executablePath = execResult.getOrThrow()
        Log.d(TAG, "Starting Stockfish process from path: $executablePath")

        return try {
            val pb = ProcessBuilder(executablePath)
            pb.redirectErrorStream(false)

            val nativeDir = context?.applicationInfo?.nativeLibraryDir
            val filesDir = context?.filesDir?.absolutePath
            val envMap = pb.environment()
            val existingLdPath = envMap["LD_LIBRARY_PATH"] ?: ""
            val searchDirs = listOfNotNull(nativeDir, filesDir).filter { File(it).isDirectory }
            if (searchDirs.isNotEmpty()) {
                val newLdPath = (searchDirs + listOf(existingLdPath)).filter { it.isNotBlank() }.joinToString(":")
                envMap["LD_LIBRARY_PATH"] = newLdPath
                Log.d(TAG, "Configured LD_LIBRARY_PATH for Stockfish process: $newLdPath")
            }

            val proc = withContext(Dispatchers.IO) {
                pb.start()
            }
            process = proc

            writer = BufferedWriter(OutputStreamWriter(proc.outputStream))
            reader = BufferedReader(InputStreamReader(proc.inputStream))

            var uciOkReceived = false
            var readyOkReceived = false

            val initSuccess = withTimeoutOrNull(5000L.milliseconds) {
                // Step 1: Send "uci\n" FIRST
                sendCommandInternal("uci\n")

                // Step 2: Read output continuously until "uciok"
                while (true) {
                    if (!proc.isAlive && reader?.ready() != true) {
                        break
                    }
                    val line = readLineWithTimeout(2000L) ?: break
                    if (line.contains("uciok")) {
                        uciOkReceived = true
                        break
                    }
                }

                if (!uciOkReceived) {
                    Log.e(TAG, "Failed to receive uciok response from Stockfish engine")
                    return@withTimeoutOrNull false
                }

                // Step 3: Send "setoption" commands if configured
                for ((key, value) in engineOptions) {
                    sendCommandInternal("setoption name $key value $value\n")
                }

                // Step 4: Send "isready\n"
                sendCommandInternal("isready\n")

                // Step 5: Read output continuously until "readyok"
                while (true) {
                    if (!proc.isAlive && reader?.ready() != true) {
                        break
                    }
                    val line = readLineWithTimeout(2000L) ?: break
                    if (line.contains("readyok")) {
                        readyOkReceived = true
                        break
                    }
                }

                readyOkReceived
            } ?: false

            logErrorStream()

            if (initSuccess) {
                isInitialized.set(true)
                Log.i(TAG, "Stockfish engine initialized successfully")
                Result.success(true)
            } else {
                val isAlive = proc.isAlive
                val exitCode = if (!isAlive) {
                    try { proc.exitValue() } catch (_: Exception) { null }
                } else null
                val stderrMsg = try {
                    proc.errorStream.bufferedReader().use { it.readText() }
                } catch (_: Exception) { "" }

                val errorMsg = "Stockfish engine failed UCI initialization handshake (uciOk=$uciOkReceived, readyOk=$readyOkReceived). Process alive=$isAlive, exitCode=$exitCode, stderr: $stderrMsg"
                Log.e(TAG, errorMsg)
                performStopEngineInternal()
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (e: Exception) {
            val stderrMsg = try {
                process?.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            } catch (_: Exception) { "" }
            Log.e(TAG, "Failed to start Stockfish process. Stderr: $stderrMsg", e)
            performStopEngineInternal()
            Result.failure(e)
        }
    }

    open suspend fun setOption(name: String, value: String): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            engineOptions[name] = value
            if (isInitialized.get() && process?.isAlive == true) {
                sendCommandInternal("setoption name $name value $value\n")
            }
        }
    }

    open suspend fun newGame(): Result<Boolean> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!isInitialized.get() || process == null || process?.isAlive != true) {
                val started = performStartEngineInternal()
                if (started.isFailure) return@withContext started
            }

            try {
                sendCommandInternal("ucinewgame\n")
                sendCommandInternal("isready\n")

                var readyOkReceived = false
                withTimeoutOrNull(3000L.milliseconds) {
                    while (true) {
                        val line = readLineWithTimeout(2000L) ?: break
                        if (line.contains("readyok")) {
                            readyOkReceived = true
                            break
                        }
                    }
                }
                Result.success(readyOkReceived)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending ucinewgame to engine", e)
                Result.failure(e)
            }
        }
    }

    private fun sendCommandInternal(command: String) {
        try {
            writer?.let {
                val cmd = if (command.endsWith("\n")) command else "$command\n"
                it.write(cmd)
                it.flush()
                Log.d(TAG, "Sent command to Stockfish: ${cmd.trimEnd()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending command: $command", e)
        }
    }

    open suspend fun setSkillLevel(level: Int) {
        val clampedLevel = level.coerceIn(0, 20)
        setOption("Skill Level", clampedLevel.toString())
    }

    open suspend fun setEloRating(elo: Int) {
        val clampedElo = elo.coerceIn(1350, 2850)
        setOption("UCI_LimitStrength", "true")
        setOption("UCI_Elo", clampedElo.toString())
    }

    open suspend fun resetToMaxStrength() {
        setOption("UCI_LimitStrength", "false")
        setOption("Skill Level", "20")
    }

    open suspend fun setDifficulty(difficulty: Difficulty) {
        if (difficulty == Difficulty.GRANDMASTER) {
            resetToMaxStrength()
        } else {
            difficulty.elo?.let { setEloRating(it) }
            setSkillLevel(difficulty.skillLevel)
        }
    }

    open suspend fun findBestMove(fen: String, difficulty: Difficulty): Result<String> {
        setDifficulty(difficulty)
        return findBestMove(fen, difficulty.depth)
    }

    open suspend fun findBestMove(fen: String, depth: Int): Result<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!isInitialized.get() || process == null || process?.isAlive != true) {
                val started = performStartEngineInternal()
                if (started.isFailure) {
                    return@withContext Result.failure(started.exceptionOrNull() ?: IllegalStateException("Engine failed to start"))
                }
            }

            try {
                val cleanFen = fen.trim()
                sendCommandInternal("position fen $cleanFen")
                sendCommandInternal("go depth $depth movetime 5000")

                var bestMove: String? = null
                val isBlackTurn = cleanFen.split(" ").getOrNull(1) == "b"

                withTimeoutOrNull(15000L.milliseconds) {
                    while (true) {
                        val line = readLineWithTimeout(15000L) ?: break

                        if (line.contains("score cp ")) {
                            val cpIndex = line.indexOf("score cp ") + 9
                            val nextSpace = line.indexOf(' ', cpIndex)
                            val cpString = if (nextSpace != -1) line.substring(cpIndex, nextSpace) else line.substring(cpIndex)
                            cpString.toIntOrNull()?.let { cp ->
                                val score = cp / 100f
                                evaluationFlow.value = if (isBlackTurn) -score else score
                            }
                        } else if (line.contains("score mate ")) {
                            val mateIndex = line.indexOf("score mate ") + 11
                            val nextSpace = line.indexOf(' ', mateIndex)
                            val mateString = if (nextSpace != -1) line.substring(mateIndex, nextSpace) else line.substring(mateIndex)
                            mateString.toIntOrNull()?.let { mate ->
                                val score = if (mate > 0) 100f else -100f
                                evaluationFlow.value = if (isBlackTurn) -score else score
                            }
                        }

                        if (line.startsWith("bestmove")) {
                            val parts = line.split(" ")
                            if (parts.size >= 2) {
                                bestMove = parts[1]
                            }
                            break
                        }
                    }
                }

                if (bestMove != null && bestMove != "(none)") {
                    Result.success(bestMove)
                } else {
                    Result.failure(IllegalStateException("No best move returned by Stockfish engine"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting best move", e)
                isInitialized.set(false)
                Result.failure(e)
            }
        }
    }

    private suspend fun readLineWithTimeout(timeoutMs: Long): String? = withContext(Dispatchers.IO) {
        logErrorStream()
        val r = reader ?: return@withContext null
        try {
            withTimeoutOrNull(timeoutMs.milliseconds) {
                r.readLine()?.also { line ->
                    Log.d(TAG, "Engine stdout: $line")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception reading from engine stdout", e)
            null
        }
    }

    private fun logErrorStream() {
        try {
            val errStream = process?.errorStream
            if (errStream != null && errStream.available() > 0) {
                val errText = errStream.bufferedReader().use { it.readText() }
                if (errText.isNotBlank()) {
                    Log.e(TAG, "Engine stderr: $errText")
                }
            }
        } catch (_: Exception) {}
    }

    open fun stopEngine() {
        try {
            runBlocking(Dispatchers.IO) {
                mutex.withLock {
                    performStopEngineInternal()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping engine", e)
            performStopEngineInternal()
        }
    }

    private fun performStopEngineInternal() {
        try {
            sendCommandInternal("quit")
        } catch (_: Exception) {}

        try {
            writer?.close()
            reader?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing engine streams", e)
        }

        process?.let { proc ->
            try {
                if (proc.isAlive) {
                    val exited = proc.waitFor(1000, TimeUnit.MILLISECONDS)
                    if (!exited) {
                        Log.w(TAG, "Stockfish process did not exit within timeout, destroying forcibly")
                        proc.destroyForcibly()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error waiting for process termination, destroying forcibly", e)
                proc.destroyForcibly()
            }
        }

        writer = null
        reader = null
        process = null
        isInitialized.set(false)
    }

    companion object {
        private const val TAG = "StockfishEngine"
    }
}

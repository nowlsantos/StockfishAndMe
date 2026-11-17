package com.nowlsantos.stockfishandme

import android.app.Application
import com.nowlsantos.stockfishandme.di.gameModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class StockfishAndMeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for Dependency Injection
        startKoin {
            androidContext(this@StockfishAndMeApp)
            androidLogger(Level.ERROR)
            modules(gameModule)
        }
    }
}
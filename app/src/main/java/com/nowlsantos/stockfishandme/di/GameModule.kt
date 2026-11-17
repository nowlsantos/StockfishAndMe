package com.nowlsantos.stockfishandme.di

import com.nowlsantos.stockfishandme.engine.StockfishEngine
import com.nowlsantos.stockfishandme.presentation.GameViewModel
import com.nowlsantos.stockfishandme.utils.SoundManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val gameModule = module {
    single { StockfishEngine(get()) }
    single { SoundManager(androidContext()) }
    viewModel { GameViewModel(get(), get()) }
}

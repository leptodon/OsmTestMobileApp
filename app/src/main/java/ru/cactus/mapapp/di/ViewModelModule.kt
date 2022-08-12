package ru.cactus.mapapp.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.cactus.mapapp.MainViewModel

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
}
package com.wdmaster.app.di

import com.wdmaster.app.ui.viewmodel.HomeViewModel
import com.wdmaster.app.ui.viewmodel.MainViewModel
import com.wdmaster.app.ui.viewmodel.SettingsViewModel
import com.wdmaster.app.ui.viewmodel.TestViewModel
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    // ViewModels are automatically provided by Hilt with @HiltViewModel annotation
    // This module is for additional ViewModel bindings if needed
    
    @ViewModelScoped
    @Provides
    fun provideMainViewModel(
        settingsRepository: com.wdmaster.app.data.repository.SettingsRepository
    ): MainViewModel {
        return MainViewModel(settingsRepository)
    }
}
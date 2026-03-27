package com.wdmaster.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    // ✅ لا تضع أي @Provides هنا للـ ViewModels
    // Hilt يتكفل بها تلقائيًا عبر @HiltViewModel
}

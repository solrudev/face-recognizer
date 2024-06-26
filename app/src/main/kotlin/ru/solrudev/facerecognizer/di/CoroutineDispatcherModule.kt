package ru.solrudev.facerecognizer.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InstallIn(SingletonComponent::class)
@Module
object CoroutineDispatcherModule {

	@Provides
	@DefaultDispatcher
	fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

	@Provides
	@MainDispatcher
	fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
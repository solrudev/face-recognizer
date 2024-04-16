package ru.solrudev.facerecognizer.di

import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.model.Model
import ru.solrudev.facerecognizer.ml.MobileFaceNet

@InstallIn(SingletonComponent::class)
@Module(includes = [FaceRecognitionBindModule::class])
object FaceRecognitionModule {

	@Provides
	fun provideMobileFaceNet(@ApplicationContext context: Context): MobileFaceNet {
		val recognizerOptionsBuilder = Model.Options.Builder()
			.setNumThreads(2)
		if (CompatibilityList().isDelegateSupportedOnThisDevice && Build.VERSION.SDK_INT < 28) {
			recognizerOptionsBuilder.setDevice(Model.Device.GPU)
		}
		if (Build.VERSION.SDK_INT >= 28) {
			recognizerOptionsBuilder.setDevice(Model.Device.NNAPI)
		}
		return MobileFaceNet.newInstance(context, recognizerOptionsBuilder.build())
	}
}
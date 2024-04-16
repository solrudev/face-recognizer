import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Locale
import java.util.Properties

val packageName = "ru.solrudev.facerecognizer"

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.ksp)
	alias(dagger.plugins.hilt.plugin)
	alias(androidx.plugins.navigation.safeargs)
	alias(libs.plugins.android.cachefix)
}

kotlin {
	jvmToolchain(17)
}

base {
	archivesName.set(packageName)
}

android {
	compileSdk = 34
	buildToolsVersion = "34.0.0"
	namespace = packageName

	defaultConfig {
		applicationId = packageName
		minSdk = 26
		targetSdk = 34
		versionCode = 1
		versionName = "0.0.1"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables.useSupportLibrary = true
	}

	val releaseSigningConfig by signingConfigs.registering {
		initWith(signingConfigs["debug"])
		val keystorePropertiesFile = rootProject.file("keystore.properties")
		if (keystorePropertiesFile.exists()) {
			val keystoreProperties = Properties().apply {
				keystorePropertiesFile.inputStream().use(::load)
			}
			keyAlias = keystoreProperties["keyAlias"] as? String
			keyPassword = keystoreProperties["keyPassword"] as? String
			storeFile = keystoreProperties["storeFile"]?.let(::file)
			storePassword = keystoreProperties["storePassword"] as? String
			enableV3Signing = true
			enableV4Signing = true
		}
	}

	buildTypes {
		named("debug") {
			multiDexEnabled = true
		}
		named("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			signingConfig = releaseSigningConfig.get()
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}

	splits {
		abi {
			isEnable = true
			reset()
			//noinspection ChromeOsAbiSupport
			include("arm64-v8a")
			isUniversalApk = false
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	buildFeatures {
		mlModelBinding = true
		viewBinding = true
	}
}

tasks.withType<KotlinJvmCompile>().configureEach {
	compilerOptions {
		freeCompilerArgs.add("-Xjvm-default=all")
	}
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

androidComponents {
	onVariants(selector().all()) { variant ->
		afterEvaluate {
			val variantName = variant.name.replaceFirstChar {
				if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
			}
			tasks.getByName<KotlinCompile>("ksp${variantName}Kotlin") {
				setSource(tasks.getByName("generate${variantName}MlModelClass").outputs)
			}
		}
	}
}

dependencies {
	ksp(dagger.bundles.hilt.compilers)

	implementation(dagger.hilt.android)
	implementation(androidx.activity.ktx)
	implementation(androidx.fragment.ktx)
	implementation(androidx.bundles.navigation)
	implementation(androidx.bundles.camera)
	implementation(libs.materialcomponents)
	implementation(libs.kotlinx.coroutines)
	implementation(libs.viewbindingpropertydelegate)
	implementation(libs.insetter)
	implementation(libs.mlkit.facedetection)
	implementation(libs.bundles.tensorflow)

	debugImplementation(androidx.multidex)

	testImplementation(libs.kotlin.test)
	testImplementation(libs.bundles.junit)
	testImplementation(libs.kotlinx.coroutines.test)
	androidTestImplementation(androidx.test.ext.junit)
	androidTestImplementation(androidx.espresso.core)
}
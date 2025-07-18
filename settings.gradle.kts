rootProject.name = "FaceRecognizer"
include(":app")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
	repositories {
		google {
			content {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	repositories {
		google {
			content {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
	}

	versionCatalogs {
		register("androidx") {
			from(files("gradle/androidx.versions.toml"))
		}
		register("dagger") {
			from(files("gradle/dagger.versions.toml"))
		}
	}
}
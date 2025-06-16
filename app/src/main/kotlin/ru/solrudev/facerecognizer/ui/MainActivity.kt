package ru.solrudev.facerecognizer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.solrudev.facerecognizer.R
import ru.solrudev.facerecognizer.databinding.MainNavHostBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.main_nav_host) {

	private val binding by viewBinding(MainNavHostBinding::bind, R.id.container_nav_host)
	private val topLevelDestinations = setOf(R.id.face_capture_fragment)
	private val appBarConfiguration = AppBarConfiguration(topLevelDestinations)

	private val navController: NavController
		get() = binding.contentNavHost.getFragment<NavHostFragment>().navController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		with(binding) {
			setContentView(root)
			applyInsets()
			toolbarNavHost.setupWithNavController(navController, appBarConfiguration)
		}
		navController.currentBackStackEntryFlow
			.flowWithLifecycle(lifecycle)
			.map { entry -> entry.destination.id == R.id.face_capture_fragment }
			.distinctUntilChanged()
			.onEach { isCameraScreen -> binding.appBarLayoutNavHost.isVisible = !isCameraScreen }
			.launchIn(lifecycleScope)
	}

	private fun MainNavHostBinding.applyInsets() {
		appBarLayoutNavHost.applyInsetter {
			type(statusBars = true) {
				padding()
			}
			type(navigationBars = true) {
				margin(horizontal = true)
			}
			type(displayCutout = true) {
				padding(left = true, top = true, right = true)
			}
		}
		contentNavHost.applyInsetter {
			type(navigationBars = true) {
				margin(horizontal = true)
			}
		}
	}
}
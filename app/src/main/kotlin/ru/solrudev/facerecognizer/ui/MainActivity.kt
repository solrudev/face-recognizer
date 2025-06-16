package ru.solrudev.facerecognizer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding
import dev.chrisbanes.insetter.applyInsetter
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
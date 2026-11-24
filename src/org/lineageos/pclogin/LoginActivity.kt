package org.lineageos.pclogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import java.util.function.Consumer
import org.lineageos.pclogin.ui.LoginActions
import org.lineageos.pclogin.ui.LoginContent
import org.lineageos.pclogin.ui.LoginTheme

/** Home screen of the headless system user: who is going to use the PC. */
class LoginActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel
    private val isBlurred = mutableStateOf(false)
    private val onBlurChanged = Consumer<Boolean> { applyBlur(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        // Home has nowhere to go back to.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })
        val actions = LoginActions(
            onChoose = viewModel::choose,
            onAddUser = viewModel::addUser,
            onStartGuest = viewModel::startGuest,
            onAccessibility = { AccessibilityLauncher.open(this) },
        )
        setContent {
            LoginTheme {
                LoginContent(viewModel.screen, viewModel.avatars, isBlurred.value, actions)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        windowManager.addCrossWindowBlurEnabledListener(mainExecutor, onBlurChanged)
    }

    override fun onDetachedFromWindow() {
        windowManager.removeCrossWindowBlurEnabledListener(onBlurChanged)
        super.onDetachedFromWindow()
    }

    private fun applyBlur(enabled: Boolean) {
        isBlurred.value = enabled
        window.setBackgroundBlurRadius(resources.getDimensionPixelSize(BLUR_RADIUS.getValue(enabled)))
    }

    private companion object {
        val BLUR_RADIUS = mapOf(true to R.dimen.login_blur_radius, false to R.dimen.login_no_blur)
    }
}

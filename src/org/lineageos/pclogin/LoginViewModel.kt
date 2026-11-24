package org.lineageos.pclogin

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.pclogin.core.LoginController
import org.lineageos.pclogin.core.LoginScreen
import org.lineageos.pclogin.core.NewUserNames

/** Runs [LoginController] on one background thread and follows user changes. */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val worker = Executors.newSingleThreadExecutor()
    private val state = MutableStateFlow(LoginScreen())
    val screen: StateFlow<LoginScreen> = state.asStateFlow()

    val avatars = UserAvatars(application)

    private val controller = LoginController(
        directory = AndroidUserDirectory(application),
        names = NewUserNames(
            user = application.getString(R.string.new_user_name),
            guest = application.getString(R.string.guest_name),
        ),
        publish = { state.value = it },
    )

    private val userChanges = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = run(controller::refresh)
    }

    init {
        val filter = IntentFilter().apply { USER_CHANGE_ACTIONS.forEach(::addAction) }
        application.registerReceiverForAllUsers(userChanges, filter, null, null, Context.RECEIVER_NOT_EXPORTED)
    }

    fun start() = run(controller::start)

    fun choose(userId: Int) = run { controller.choose(userId) }

    fun addUser() = run(controller::addUser)

    fun startGuest() = run(controller::startGuest)

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(userChanges)
        worker.shutdown()
    }

    private fun run(action: () -> Unit) = worker.execute(action)

    private companion object {
        val USER_CHANGE_ACTIONS = listOf(
            Intent.ACTION_USER_ADDED,
            Intent.ACTION_USER_REMOVED,
            Intent.ACTION_USER_INFO_CHANGED,
            Intent.ACTION_USER_SWITCHED,
        )
    }
}

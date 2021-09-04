package io.simplelogin.android.module.startup

import android.app.Activity
import android.content.Intent
import android.net.MailTo
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.login.LoginActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.UserInfo

class StartupActivity : BaseAppCompatActivity()  {
    companion object {
        const val RC_LOGIN_ACTIVITY = 0
        const val RC_HOME_ACTIVITY = 1
        const val RC_LOCAL_AUTH_ACTIVITY = 2
    }

    private lateinit var binding: ActivityStartUpBinding
    private var shouldLocallyAuthenticate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shouldLocallyAuthenticate = SLSharedPreferences.getShouldLocallyAuthenticate(this)
    }

    override fun onResume() {
        super.onResume()
        when (SLSharedPreferences.getApiKey(this)) {
            null -> startLoginActivity()
            else -> {
                if (shouldLocallyAuthenticate) {
                    startLocalAuthActivity()
                } else {
                    fetchUserInfoAndProceed()
                }
            }
        }
    }

    override fun onBackPressed() = Unit

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, RC_LOGIN_ACTIVITY)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun startLocalAuthActivity() {
        val intent = Intent(this, LocalAuthActivity::class.java)
        startActivityForResult(intent, RC_LOCAL_AUTH_ACTIVITY)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun startHomeActivity(userInfo: UserInfo) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.USER_INFO, userInfo)
        getMailToFromIntent()?.to.let { intent.putExtra(HomeActivity.EMAIL, it) }
        startActivityForResult(intent, RC_HOME_ACTIVITY)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun fetchUserInfoAndProceed() {
        val apiKey = SLSharedPreferences.getApiKey(this)
        if (apiKey == null) {
            startLoginActivity()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        SLApiService.fetchUserInfo(apiKey) { result ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                result.onSuccess(::startHomeActivity)
                result.onFailure(::handleError)
            }
        }
    }

    private fun handleError(error: Throwable) {
        when (error as SLError) {
            SLError.InvalidApiKey -> startLoginActivity()
            else ->
                Snackbar.make(binding.bottomCoordinatorLayout, error.description, Snackbar.LENGTH_INDEFINITE).apply {
                    setAction("Retry") { fetchUserInfoAndProceed() }
                }.show()
        }
    }

    private fun getMailToFromIntent(): MailTo? {
        // App launched from mailto: link
        val action = intent.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_SENDTO) {
            val uri = intent.data
            return MailTo.parse(uri.toString())
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_LOGIN_ACTIVITY, RC_HOME_ACTIVITY ->
                if (resultCode == Activity.RESULT_CANCELED) {
                    // Exit application when backed from LoginActivity or HomeActivity
                    finish()
                }

            RC_LOCAL_AUTH_ACTIVITY ->
                if (resultCode == Activity.RESULT_OK) {
                    shouldLocallyAuthenticate = false
                }
        }
    }
}

package io.simplelogin.android.module.startup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
        const val RC_HOME_ACTIVITY = 0
    }

    private lateinit var binding: ActivityStartUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SLApiService.setUpBaseUrl(this)
    }

    override fun onResume() {
        super.onResume()

        val apiKey = SLSharedPreferences.getApiKey(this)
        if (apiKey != null) {
            fetchUserInfoAndProceed(apiKey)
        } else {
            startLoginActivity()
        }
    }

    override fun onBackPressed() = Unit

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun startHomeActivity(userInfo: UserInfo) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.USER_INFO, userInfo)
        startActivityForResult(intent, RC_HOME_ACTIVITY)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun fetchUserInfoAndProceed(apiKey: String) {
        SLApiService.fetchUserInfo(apiKey) { result ->
            runOnUiThread {
                result.onSuccess(::startHomeActivity)

                result.onFailure {
                    val error = it as SLError
                    showErrorSnackBar(error)
                    if (error == SLError.InvalidApiKey) {
                        startLoginActivity()
                    }
                }
            }
        }
    }

    private fun showErrorSnackBar(error: SLError) {
        Snackbar.make(binding.bottomCoordinatorLayout, error.description, Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                SLSharedPreferences.getApiKey(this)?.let { apiKey ->

                    fetchUserInfoAndProceed(apiKey)
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_HOME_ACTIVITY -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    // Exit application when backed from HomeActivity
                    finish()
                }
            }
        }
    }
}
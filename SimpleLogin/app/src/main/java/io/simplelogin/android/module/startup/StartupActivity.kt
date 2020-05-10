package io.simplelogin.android.module.startup

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
    private lateinit var binding: ActivityStartUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        startActivity(intent)
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
}
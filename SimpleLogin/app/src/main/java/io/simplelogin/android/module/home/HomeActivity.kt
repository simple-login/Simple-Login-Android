package io.simplelogin.android.module.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import io.simplelogin.android.R.*
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.model.UserInfo

class HomeActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val USER_INFO = "userInfo"
    }
    lateinit var binding: ActivityHomeBinding
        private set
    private lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve UserInfo from intent
        userInfo = intent.getParcelableExtra<UserInfo>(USER_INFO)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        binding.navigationView.setNavigationItemSelectedListener(this)
        setUpDrawer()
        setContentView(binding.root)

        // Change status bar background color
        window.statusBarColor = ContextCompat.getColor(this, color.colorWhite)
    }

    override fun onBackPressed() = Unit

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(anim.stay_still, anim.slide_out_down)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.aliasMenuItem -> {
                Log.d("item", "alias")
            }

            id.settingsMenuItem -> {
                Log.d("item", "settings")
            }

            id.aboutMenuItem -> {
                Log.d("item", "about")
            }

            id.signOutMenuItem -> {
                // Sign Out
                MaterialAlertDialogBuilder(this)
                    .setTitle("Please confirm")
                    .setMessage("You will be signed out")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes, sign me out") { _, _ ->
                        SLSharedPreferences.removeApiKey(this)
                        finish()
                    }
                    .show()
            }
        }
        return true
    }

    private fun setUpDrawer() {
        // App version name
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        val appVersionMenuItem = binding.navigationView.menu.findItem(id.appVersionMenuItem)
        appVersionMenuItem?.title = "SimpleLogin v${packageInfo.versionName}"
        appVersionMenuItem.isEnabled = false

        // Header info
        val avatarImageView = binding.navigationView.getHeaderView(0).findViewById<ImageView>(id.avatarImageView)

        val usernameTextView = binding.navigationView.getHeaderView(0).findViewById<TextView>(id.usernameTextView)
        usernameTextView.text = userInfo.name

        val statusTextView = binding.navigationView.getHeaderView(0).findViewById<TextView>(id.statusTextView)
        if (userInfo.isPremium) {
            statusTextView.text = "Premium"
            statusTextView.setTextColor(ContextCompat.getColor(this, color.colorPremium))
        } else {
            statusTextView.text = "Freemium"
            statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }
}
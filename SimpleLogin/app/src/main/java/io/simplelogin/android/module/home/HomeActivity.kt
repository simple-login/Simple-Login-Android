package io.simplelogin.android.module.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.getVersionName
import io.simplelogin.android.utils.model.UserInfo

class HomeActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    interface OnBackPressed {
        fun onBackPressed()
    }

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
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.size == 0) return
        val navHostFragment = (supportFragmentManager.fragments[0] as? NavHostFragment) ?: return

        for (fragment in navHostFragment.childFragmentManager.fragments) {
            if (fragment is OnBackPressed) {
                (fragment as OnBackPressed).onBackPressed()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        val navInflater = navController.navInflater

        when (item.itemId) {
            R.id.aliasMenuItem -> {
                val aliasNavGraph = navInflater.inflate(R.navigation.nav_graph_alias)
                navController.graph = aliasNavGraph
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.settingsMenuItem -> {
                val settingsNavGraph = navInflater.inflate(R.navigation.nav_graph_settings)
                settingsNavGraph.addArgument(USER_INFO, NavArgument.Builder().setDefaultValue(userInfo).build())
                navController.graph = settingsNavGraph
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.aboutMenuItem -> {
                val aboutNavGraph = navInflater.inflate(R.navigation.nav_graph_about)
                navController.graph = aboutNavGraph
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.signOutMenuItem -> {
                // Sign Out
                MaterialAlertDialogBuilder(this)
                    .setTitle("You will be signed out")
                    .setMessage("Please confirm")
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton("Yes, sign me out") { _, _ ->
                        SLSharedPreferences.removeApiKey(this)
                        finish()
                    }
                    .show()
            }
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDrawer() {
        // App version name
        val appVersionMenuItem = binding.navigationView.menu.findItem(R.id.appVersionMenuItem)
        appVersionMenuItem?.title = "SimpleLogin v${getVersionName()}"
        appVersionMenuItem.isEnabled = false

        // Header info
        val headerView = binding.navigationView.getHeaderView(0)
//        val avatarImageView = headerView.findViewById<ImageView>(R.id.avatarImageView)

        val usernameTextView = headerView.findViewById<TextView>(R.id.usernameTextView)
        usernameTextView.text = userInfo.name

        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)
        emailTextView.text = userInfo.email

        val membershipTextView = headerView.findViewById<TextView>(R.id.membershipTextView)
        if (userInfo.inTrial) {
            membershipTextView.text = "Premium trial"
            membershipTextView.setTextColor(getColor(android.R.color.holo_blue_light))
        } else if (userInfo.isPremium) {
            membershipTextView.text = "Premium"
            membershipTextView.setTextColor(getColor(R.color.colorPremium))
        } else {
            membershipTextView.text = "Free plan"
            membershipTextView.setTextColor(getColor(R.color.colorDarkGray))
        }
    }
}
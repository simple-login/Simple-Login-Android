package io.simplelogin.android.module.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.module.about.AboutFragment
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
        if (binding.mainDrawer.isDrawerOpen(binding.navigationView)) {
            // When navigationView is already open and user press back
            // finish this activity with RESULT_CANCELED so that StartupActivity can finish itself to exit app
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

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
                navController.graph = navInflater.inflate(R.navigation.nav_graph_alias)
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.mailboxMenuItem -> {
                navController.graph = navInflater.inflate(R.navigation.nav_graph_mailbox)
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.settingsMenuItem -> {
                val settingsNavGraph = navInflater.inflate(R.navigation.nav_graph_settings)
                settingsNavGraph.addArgument(
                    USER_INFO,
                    NavArgument.Builder().setDefaultValue(userInfo).build()
                )
                navController.graph = settingsNavGraph
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.aboutMenuItem -> {
                val aboutNavGraph = navInflater.inflate(R.navigation.nav_graph_about)
                aboutNavGraph.addArgument(
                    AboutFragment.OPEN_FROM_LOGIN_ACTIVITY,
                    NavArgument.Builder().setDefaultValue(false).build()
                )
                navController.graph = aboutNavGraph
                binding.mainDrawer.closeDrawer(Gravity.LEFT)
            }

            R.id.rateUsMenuItem -> {
                val uri = Uri.parse("market://details?id=$packageName")
                val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)

                goToMarketIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

                try {
                    SLSharedPreferences.setRated(this, true)
                    startActivity(goToMarketIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
                }
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

        when {
            userInfo.inTrial -> {
                membershipTextView.text = "Premium trial"
                membershipTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
            }

            userInfo.isPremium -> {
                membershipTextView.text = "Premium"
                membershipTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPremium))
            }

            else -> {
                membershipTextView.text = "Free plan"
                membershipTextView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
        }

        // Define a nested function in order to reuse it later
        fun hideRateUsMenuItemIfApplicable() {
            binding.navigationView.menu.findItem(R.id.rateUsMenuItem).isVisible =
                !SLSharedPreferences.getRated(this)
        }

        hideRateUsMenuItemIfApplicable()

        // Add listener to properly hide "Rate us" menu item
        binding.mainDrawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit
            override fun onDrawerOpened(drawerView: View) = Unit
            override fun onDrawerStateChanged(newState: Int) = Unit

            override fun onDrawerClosed(drawerView: View) {
                hideRateUsMenuItemIfApplicable()
            }
        })
    }
}
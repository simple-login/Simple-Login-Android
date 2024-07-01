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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.module.about.AboutFragment
import io.simplelogin.android.module.settings.SettingsFragment
import io.simplelogin.android.module.settings.view.AvatarView
import io.simplelogin.android.module.startup.StartupActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.getVersionName
import io.simplelogin.android.utils.model.UserInfo

class HomeActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    interface OnBackPressed {
        fun onBackPressed()
    }

    enum class NavigationGraph {
        ALIAS, MAILBOX, SETTINGS, ABOUT
    }

    companion object {
        const val EMAIL = "email"
        const val USER_INFO = "userInfo"
    }

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewModel()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        binding.navigationView.setNavigationItemSelectedListener(this)
        setUpDrawer()
        setContentView(binding.root)
        setNavigationGraph(viewModel.navigationGraph)
    }

    private fun setUpViewModel() {
        viewModel.eventUserInfoUpdated.observe(this) { updated ->
            if (updated) {
                updateHeaderView()
                viewModel.onHandleUserInfoUpdateComplete()
            }
        }
        // Retrieve UserInfo from intent
        val userInfo = intent.getParcelableExtra(USER_INFO) as? UserInfo
            ?: throw IllegalStateException("UserInfo can not be null")
        viewModel.setUserInfo(userInfo)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // In case we received a new intent is because the Connect with Proton worked
        // Refresh the user info
        if (supportFragmentManager.fragments.size == 0) return
        val navHostFragment = supportFragmentManager.fragments[0] as? NavHostFragment ?: return
        val settingsFragment = navHostFragment.childFragmentManager.fragments.find { it is SettingsFragment }
        if (settingsFragment != null) {
            val casted = settingsFragment as SettingsFragment
            casted.onNewIntent(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.mainDrawer.isDrawerOpen(binding.navigationView)) {
            // When navigationView is already open and user press back
            // finish this activity with RESULT_CANCELED so that StartupActivity can finish itself to exit app
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        if (supportFragmentManager.fragments.size == 0) return
        val navHostFragment = supportFragmentManager.fragments[0] as? NavHostFragment ?: return

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

    private fun setNavigationGraph(navigationGraph: NavigationGraph) {
        val navController = findNavController(R.id.homeNavHostFragment)
        val navInflater = navController.navInflater
        when (navigationGraph) {
            NavigationGraph.ALIAS -> navController.graph = navInflater.inflate(R.navigation.nav_graph_alias)
            NavigationGraph.MAILBOX -> navController.graph = navInflater.inflate(R.navigation.nav_graph_mailbox)

            NavigationGraph.SETTINGS -> {
                val settingsNavGraph = navInflater.inflate(R.navigation.nav_graph_settings)
                settingsNavGraph.addArgument(
                    USER_INFO,
                    NavArgument.Builder().setDefaultValue(viewModel.userInfo).build()
                )
                navController.graph = settingsNavGraph
            }

            NavigationGraph.ABOUT -> {
                val aboutNavGraph = navInflater.inflate(R.navigation.nav_graph_about)
                aboutNavGraph.addArgument(
                    AboutFragment.OPEN_FROM_LOGIN_ACTIVITY,
                    NavArgument.Builder().setDefaultValue(false).build()
                )
                navController.graph = aboutNavGraph
            }
        }
        viewModel.navigationGraph = navigationGraph
        binding.mainDrawer.closeDrawer(Gravity.LEFT)
    }

    @SuppressLint("RtlHardcoded")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aliasMenuItem -> setNavigationGraph(NavigationGraph.ALIAS)
            R.id.mailboxMenuItem -> setNavigationGraph(NavigationGraph.MAILBOX)
            R.id.settingsMenuItem -> setNavigationGraph(NavigationGraph.SETTINGS)
            R.id.aboutMenuItem -> setNavigationGraph(NavigationGraph.ABOUT)

            R.id.rateUsMenuItem -> {
                val uri = Uri.parse("market://details?id=$packageName")
                val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)

                @Suppress("MaxLineLength")
                goToMarketIntent.flags =
                    Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

                try {
                    SLSharedPreferences.setRated(this, true)
                    startActivity(goToMarketIntent)
                } catch (_: ActivityNotFoundException) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                    startActivity(intent)
                }
            }

            R.id.signOutMenuItem -> {
                // Sign Out
                MaterialAlertDialogBuilder(this)
                    .setTitle("You will be signed out")
                    .setMessage("Please confirm")
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton("Yes, sign me out") { _, _ -> resetSettingsAndRestartApp() }
                    .show()
            }
        }
        return true
    }

    private fun resetSettingsAndRestartApp() {
        SLSharedPreferences.reset(this)
        val intent = Intent(this, StartupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDrawer() {
        // App version name
        val appVersionMenuItem = binding.navigationView.menu.findItem(R.id.appVersionMenuItem)
        appVersionMenuItem?.title = "SimpleLogin v${getVersionName()}"
        appVersionMenuItem.isEnabled = false

        // Header info
        updateHeaderView()

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

    fun openDrawer() {
        binding.mainDrawer.openDrawer(GravityCompat.START)
    }

    private fun updateHeaderView() {
        val headerView = binding.navigationView.getHeaderView(0)
        val avatarView = headerView.findViewById<AvatarView>(R.id.avatar_view)
        avatarView.setAvatar(viewModel.userInfo.profilePhotoUrl)

        val usernameTextView = headerView.findViewById<TextView>(R.id.usernameTextView)
        usernameTextView.text = viewModel.userInfo.name

        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)
        emailTextView.text = viewModel.userInfo.email

        val membershipTextView = headerView.findViewById<TextView>(R.id.membershipTextView)

        when {
            viewModel.userInfo.inTrial -> {
                membershipTextView.text = "Premium trial"
                membershipTextView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_blue_light
                    )
                )
            }

            viewModel.userInfo.isPremium -> {
                membershipTextView.text = "Premium"
                membershipTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPremium))
            }

            else -> {
                membershipTextView.text = "Free plan"
                membershipTextView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
            }
        }
    }
}

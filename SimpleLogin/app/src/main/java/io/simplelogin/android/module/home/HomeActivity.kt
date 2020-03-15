package io.simplelogin.android.module.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import com.google.android.material.navigation.NavigationView
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class HomeActivity : BaseAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityHomeBinding
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        binding.navigationView.setNavigationItemSelectedListener(this)
        setUpDrawer()
        setContentView(binding.root)

        // Change status bar background color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite)
    }

    override fun onBackPressed() = Unit

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    private fun setUpDrawer() {
        binding.navigationView.menu.iterator().forEach {  }
    }
}
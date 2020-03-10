package io.simplelogin.android.module.home

import android.os.Bundle
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class HomeActivity : BaseAppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() = Unit

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }
}
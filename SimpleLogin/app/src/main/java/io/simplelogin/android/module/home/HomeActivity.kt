package io.simplelogin.android.module.home

import android.os.Bundle
import io.simplelogin.android.databinding.ActivityHomeBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class HomeActivity : BaseAppCompatActivity() {
    companion object {
        lateinit var binding: ActivityHomeBinding
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
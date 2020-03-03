package io.simplelogin.android.module.startup

import android.os.Bundle
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class StartupActivity : BaseAppCompatActivity()  {

    companion object {
        lateinit var binding: ActivityStartUpBinding
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
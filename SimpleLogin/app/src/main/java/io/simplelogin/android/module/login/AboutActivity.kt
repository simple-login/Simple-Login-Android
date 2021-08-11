package io.simplelogin.android.module.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityAboutBinding
import io.simplelogin.android.module.about.AboutFragment

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findNavController(R.id.aboutNavHostFragment).graph.addArgument(
            AboutFragment.OPEN_FROM_LOGIN_ACTIVITY,
            NavArgument.Builder().setDefaultValue(true).build()
        )
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }
}

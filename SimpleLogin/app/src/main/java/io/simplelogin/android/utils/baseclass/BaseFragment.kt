package io.simplelogin.android.utils.baseclass

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import io.simplelogin.android.module.home.HomeActivity

open class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showLeftMenu() {
        val homeActivity = activity as? HomeActivity
        homeActivity?.let {
            it.binding.mainDrawer.openDrawer(Gravity.START)
        }
    }
}
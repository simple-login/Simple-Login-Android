package io.simplelogin.android.utils.baseclass

import android.view.Gravity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import io.simplelogin.android.module.home.HomeActivity

open class BaseFragment : Fragment() {
    fun showLeftMenu() {
        val homeActivity = activity as? HomeActivity
        homeActivity?.binding?.mainDrawer?.openDrawer(GravityCompat.START)
    }
}

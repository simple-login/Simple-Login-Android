package io.simplelogin.android.utils.baseclass

import android.view.Gravity
import androidx.fragment.app.Fragment
import io.simplelogin.android.module.home.HomeActivity

open class BaseFragment : Fragment() {
    fun showLeftMenu() {
        val homeActivity = activity as? HomeActivity
        homeActivity?.let {
            it.binding.mainDrawer.openDrawer(Gravity.START)
        }
    }
}

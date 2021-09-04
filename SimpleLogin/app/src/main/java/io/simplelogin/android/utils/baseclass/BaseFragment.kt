package io.simplelogin.android.utils.baseclass

import androidx.fragment.app.Fragment
import io.simplelogin.android.module.home.HomeActivity

open class BaseFragment : Fragment() {
    fun showLeftMenu() {
        (activity as? HomeActivity)?.openDrawer()
    }
}

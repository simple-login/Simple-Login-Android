package io.simplelogin.android.utils.baseclass

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import io.simplelogin.android.module.home.HomeActivity

open class BaseFragment : Fragment() {
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = when {
            activity != null -> FirebaseAnalytics.getInstance(requireActivity())
            context != null -> FirebaseAnalytics.getInstance(requireContext())
            else -> throw Exception("Can not initialize FirebaseAnalytics instance in ${this.javaClass.canonicalName}. Activity or context is null")
        }
    }

    fun showLeftMenu() {
        val homeActivity = activity as? HomeActivity
        homeActivity?.let {
            it.binding.mainDrawer.openDrawer(Gravity.LEFT)
        }
    }
}
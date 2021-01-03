package io.simplelogin.android.module.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentSettingsBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.model.UserInfo
import io.simplelogin.android.utils.model.UserSettings

class SettingsFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        // UserInfo
        binding.profileInfoCardView.visibility = GONE
        val userInfo =
            findNavController().graph.arguments.getValue(HomeActivity.USER_INFO).defaultValue as UserInfo
        bind(userInfo)

        // Dark mode
        binding.forceDarkModeCardView.visibility = GONE
        bindForceDarkMode()

        // Other options
        binding.newslettersCardView.visibility = GONE
        setUpViewModel()
        viewModel.fetchUserSettingsAndDomainLites()

        return binding.root
    }

    private fun bind(userInfo: UserInfo) {
        binding.profileInfoCardView.visibility = VISIBLE
        binding.profileInfoCardView.bind(userInfo)
        binding.profileInfoCardView.setOnUpgradeClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToPremiumFragment()
            )
        }
    }

    private fun bind(userSettings: UserSettings) {
        // Newsletters
        binding.newslettersCardView.visibility = VISIBLE
        binding.newslettersCardView.bind(userSettings.notification)
        binding.newslettersCardView.setOnSwitchChangedListener { isChecked ->
            val option = UserSettings.Option.NotificationOption(isChecked)
            viewModel.updateUserSettings(option)
        }
    }

    private fun bindForceDarkMode() {
        binding.forceDarkModeCardView.visibility = VISIBLE
        val isChecked = SLSharedPreferences.getShouldForceDarkMode(requireContext())
        binding.forceDarkModeCardView.bind(isChecked)
        binding.forceDarkModeCardView.setOnSwitchChangedListener { shouldForceDarkMode ->
            this.context?.let { SLSharedPreferences.setShouldForceDarkMode(it, shouldForceDarkMode) }

            if (shouldForceDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }

            val intent: Intent? = context?.packageName?.let {
                context?.packageManager
                    ?.getLaunchIntentForPackage(it)
            }
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun setUpViewModel() {
        viewModel = SettingsViewModel(requireContext())

        viewModel.isFetching.observe(viewLifecycleOwner, { isFetching ->  setLoading(isFetching) })

        viewModel.error.observe(viewLifecycleOwner, {
            it?.let { error ->
                viewModel.onHandleErrorComplete()
                context?.toastError(error)
                findNavController().navigateUp()
            }
        })

        viewModel.evenUserSettingsUpdated.observe(viewLifecycleOwner, { updated ->
            if (updated) {
                bind(viewModel.userSettings)
                viewModel.onHandleUserSettingsUpdatedComplete()
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) VISIBLE else GONE
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        showLeftMenu()
    }
}

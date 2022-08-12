package io.simplelogin.android.module.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.DialogViewEditTextBinding
import io.simplelogin.android.databinding.FragmentSettingsBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.home.HomeViewModel
import io.simplelogin.android.utils.LoginWithProtonUtils
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.runOnUiThread
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.extension.toastShortly
import io.simplelogin.android.utils.model.UserInfo
import io.simplelogin.android.utils.model.UserSettings
import java.io.ByteArrayOutputStream

class SettingsFragment : BaseFragment(), HomeActivity.OnBackPressed {
    companion object {
        private const val PICK_PHOTO_REQUEST_CODE = 1000
        private const val PHOTO_LIBRARY_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private val homeViewModel: HomeViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        // Profile info
        binding.profileInfoCardView.setOnModifyClickListener { alertModificationOptions() }

        // Dark mode
        binding.forceDarkModeCardView.visibility = GONE
        bindForceDarkMode()

        // Local Authentication
        (context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.let {
            if (it.isDeviceSecure) {
                binding.localAuthenticationView.setOnSwitchChangedListener { isChecked ->
                    SLSharedPreferences.setShouldLocallyAuthenticate(requireContext(), isChecked)
                }
                binding.localAuthenticationView.bind(SLSharedPreferences.getShouldLocallyAuthenticate(requireContext()))
            } else {
                binding.localAuthenticationView.visibility = GONE
            }
        }

        // Other options
        binding.newslettersCardView.visibility = GONE
        binding.randomAliasCardView.visibility = GONE
        binding.senderAddressFormatCardView.visibility = GONE
        binding.deleteAccountCardView.visibility = GONE
        setUpViewModel()
        viewModel.fetchUserSettingsAndDomainLites()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.contactsAccessView.updateSwitchState()
    }

    fun onNewIntent(intent: Intent?) {
        // Update the user info
        viewModel.refreshUserInfo {
            runOnUiThread {
                Toast.makeText(requireContext(), R.string.your_proton_account_has_been_linked, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bind(userInfo: UserInfo) {
        binding.profileInfoCardView.visibility = VISIBLE
        binding.profileInfoCardView.bind(userInfo)
        binding.profileInfoCardView.setOnUpgradeClickListener {
            findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToPremiumFragment()
            )
        }

        // Connected with Proton
        binding.connectWithProtonCardView.bind(userInfo.connectedProtonAddress)
        binding.connectWithProtonCardView.setOnConnectButtonClickListener {
            viewModel.getTemporaryToken { token ->
                context?.let { context ->
                    LoginWithProtonUtils.launchLinkWithProton(context, token)
                }
            }
        }

        binding.connectWithProtonCardView.setOnUnlinkButtonClickListener {
            alertUnlinkProtonAccount {
                viewModel.unlinkProtonAccount()
            }
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

        // Random mode & Default domain
        binding.randomAliasCardView.visibility = VISIBLE
        binding.randomAliasCardView.bind(
                viewModel.userSettings.randomMode,
                viewModel.userSettings.randomAliasDefaultDomain,
                viewModel.domainLites
        )

        binding.randomAliasCardView.setRandomModeSpinnerSelectionListener { selectedRandomMode ->
            if (selectedRandomMode != viewModel.userSettings.randomMode) {
                val option = UserSettings.Option.RandomModeOption(selectedRandomMode)
                viewModel.updateUserSettings(option)
            }
        }

        binding.randomAliasCardView.setDefaultDomainSpinnerSelectionListener { selectedDomainLite ->
            if (selectedDomainLite.name != viewModel.userSettings.randomAliasDefaultDomain) {
                val option = UserSettings.Option.RandomAliasDefaultDomainOption(selectedDomainLite.name)
                viewModel.updateUserSettings(option)
            }
        }

        // Sender address format
        binding.senderAddressFormatCardView.visibility = VISIBLE
        binding.senderAddressFormatCardView.bind(viewModel.userSettings.senderFormat)
        binding.senderAddressFormatCardView.setSenderAddressFormatSpinnerSelectionListener { selectedSenderFormat ->
            if (selectedSenderFormat != viewModel.userSettings.senderFormat) {
                val option = UserSettings.Option.SenderFormatOption(selectedSenderFormat)
                viewModel.updateUserSettings(option)
            }
        }

        // Delete account
        binding.deleteAccountCardView.visibility = VISIBLE
        binding.deleteAccountCardView.setDeleteAccountClickListener {
            val baseUrl = SLSharedPreferences.getApiUrl(requireContext())
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("${baseUrl}/dashboard/delete_account")
            startActivity(intent)
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

        viewModel.isFetching.observe(viewLifecycleOwner) { isFetching -> setLoading(isFetching) }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { error ->
                viewModel.onHandleErrorComplete()
                context?.toastError(error)
                findNavController().navigateUp()
            }
        }

        viewModel.eventUserInfoUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                bind(viewModel.userInfo)
                homeViewModel.setUserInfo(viewModel.userInfo)
                viewModel.onHandleUserInfoUpdatedComplete()
            }
        }
        val userInfo =
                findNavController().graph.arguments.getValue(HomeActivity.USER_INFO).defaultValue as UserInfo
        viewModel.setUserInfo(userInfo)

        viewModel.evenUserSettingsUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                bind(viewModel.userSettings)
                viewModel.onHandleUserSettingsUpdatedComplete()
            }
        }

        viewModel.eventProtonAccountUnlinked.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                Toast.makeText(requireContext(), R.string.your_proton_account_has_been_unlinked, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun alertModificationOptions() {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
                .setTitle("Modify profile")
                .setItems(
                        arrayOf("Modify profile photo", "Modify display name")
                ) { _, itemIndex ->
                    when (itemIndex) {
                        0 -> alertProfilePhotoModificationOptions()
                        1 -> alertModifyDisplayName()
                    }
                }
                .show()
    }

    private fun alertProfilePhotoModificationOptions() {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
                .setTitle("Modify profile photo")
                .setItems(
                        arrayOf("Upload new photo", "Remove profile photo")
                ) { _, itemIndex ->
                    when (itemIndex) {
                        0 -> askForPhotoLibraryPermission()
                        1 -> viewModel.removeProfilePhoto()
                    }
                }
                .show()
    }

    private fun alertUnlinkProtonAccount(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext(), R.style.SlAlertDialogTheme)
            .setTitle(R.string.proton_unlink_account_confirmation_title)
            .setMessage(R.string.proton_unlink_account_confirmation_content)
            .setNegativeButton(R.string.dialog_cancel_button, null)
            .setPositiveButton(R.string.dialog_confirm_button) { _, _ -> onConfirm()}
            .show()
    }

    private fun askForPhotoLibraryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (requireActivity().checkSelfPermission(readPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(readPermission), PHOTO_LIBRARY_PERMISSION_REQUEST_CODE)
            } else {
                openPhotoPicker()
            }
        } else {
            openPhotoPicker()
        }
    }

    private fun openPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE)
    }

    private fun alertModifyDisplayName() {
        val dialogTextViewBinding = DialogViewEditTextBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter new display name")
                .setView(dialogTextViewBinding.root)
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Save") { _, _ ->
                    setLoading(true)
                    val text = dialogTextViewBinding.editText.text
                    val name = if (text.isNullOrEmpty()) null else text.toString()
                    viewModel.updateName(name)
                }
                .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.rootConstraintLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) VISIBLE else GONE
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        showLeftMenu()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PHOTO_LIBRARY_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                openPhotoPicker()
            } else {
                requireContext().toastShortly("Permission denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_PHOTO_REQUEST_CODE ->
                    data?.data?.let { uri ->
                        val input = activity?.contentResolver?.openInputStream(uri)
                        val image = BitmapFactory.decodeStream(input)
                        val baos = ByteArrayOutputStream()
                        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val byteArray = baos.toByteArray()
                        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                        viewModel.updateProfilePhoto(base64String)
                    }
            }
        }
    }
}
